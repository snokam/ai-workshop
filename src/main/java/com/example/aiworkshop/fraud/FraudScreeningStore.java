package com.example.aiworkshop.fraud;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class FraudScreeningStore {
    private final Map<String, FraudScreening> byDocumentId = new ConcurrentHashMap<>();

    public void save(FraudScreening screening) {
        byDocumentId.put(screening.documentId(), screening);
    }

    public Optional<FraudScreening> findByDocumentId(String documentId) {
        return Optional.ofNullable(byDocumentId.get(documentId));
    }

    public List<FraudScreening> findAllFor(List<String> documentIds) {
        return documentIds.stream()
                .map(byDocumentId::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
