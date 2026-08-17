package com.example.aiworkshop.fraud;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Screenings, kept against the Document they were made for. In memory; lost on restart, like
 * everything else here.
 *
 * <p>Separate from {@code DocumentStore} on purpose. Everything in that store is served to the
 * Claimant's screen; nothing in this one ever is. Keeping them apart is what makes "handler-side
 * only" a property of the code rather than a promise about how the endpoints are written.
 */
@Component
public class FraudScreeningStore {

    private final Map<String, FraudScreening> byDocumentId = new ConcurrentHashMap<>();

    public void save(FraudScreening screening) {
        byDocumentId.put(screening.documentId(), screening);
    }

    public Optional<FraudScreening> findByDocumentId(String documentId) {
        return Optional.ofNullable(byDocumentId.get(documentId));
    }

    /** The Screenings for a set of Documents, skipping any that has none. */
    public List<FraudScreening> findAllFor(List<String> documentIds) {
        return documentIds.stream()
                .map(byDocumentId::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
