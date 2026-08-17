package com.example.aiworkshop.fraud;

import com.example.aiworkshop.fraud.FraudIndicator.Kind;
import com.example.aiworkshop.fraud.FraudIndicator.Weight;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
class DuplicateUploadCheck implements FraudCheck {
    private final Map<String, List<Seen>> byHash = new ConcurrentHashMap<>();

    @Override
    public List<FraudIndicator> screen(ScreenedFile file) {
        String hash = sha256(file.content());
        List<Seen> earlier = byHash.computeIfAbsent(hash, key -> new CopyOnWriteArrayList<>());

        List<FraudIndicator> found = earlier.isEmpty() ? List.of() : List.of(indicatorFor(file, earlier));
        earlier.add(new Seen(file.documentId(), file.caseId(), file.filename()));
        return found;
    }

    private static FraudIndicator indicatorFor(ScreenedFile file, List<Seen> earlier) {
        boolean anotherCase = earlier.stream().anyMatch(seen -> !seen.caseId().equals(file.caseId()));
        return FraudIndicator.of(
                Kind.ALREADY_UPLOADED,
                anotherCase ? Weight.STRONG : Weight.NOTE,
                anotherCase
                        ? "The same file, byte for byte, has already been uploaded to a different case."
                        : "The same file has already been uploaded to this case.",
                earlier.stream()
                        .map(seen -> "%s, uploaded to case %s as document %s"
                                .formatted(seen.filename(), seen.caseId(), seen.documentId()))
                        .toList());
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }

    private record Seen(String documentId, String caseId, String filename) {}
}
