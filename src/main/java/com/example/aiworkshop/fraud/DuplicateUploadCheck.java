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

/**
 * Have these exact bytes arrived before?
 *
 * <p>The cheapest check here and the one that needs nothing outside the process: a SHA-256 of the
 * upload, looked up against every upload since the application started. It catches the same receipt
 * claimed on two Cases, and it catches a file a handler has already turned down being sent back
 * unchanged in the hope of a different reader.
 *
 * <p>Weighting turns on <em>where</em> the twin was. The same file on a different Case is the
 * interesting one — one expense, two claims. The same file on the same Case is usually somebody
 * double-clicking, so it is a {@link Weight#NOTE}: worth recording, not worth a handler's afternoon.
 *
 * <p>Byte-identical only, which is the honest limit of it. Re-saving a photo defeats this
 * completely, and a perceptual hash is the obvious next check for anyone extending the package.
 */
@Component
class DuplicateUploadCheck implements FraudCheck {

    /** Content hash to the Documents it has been seen on, oldest first. */
    private final Map<String, List<Seen>> byHash = new ConcurrentHashMap<>();

    @Override
    public List<FraudIndicator> screen(ScreenedFile file) {
        String hash = sha256(file.content());
        List<Seen> earlier = byHash.computeIfAbsent(hash, key -> new CopyOnWriteArrayList<>());

        // Recorded after the lookup and before returning, so a file never finds itself. The check
        // both reads and writes the record it screens against; there is nowhere else for that
        // memory to live while Documents keep no bytes.
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
