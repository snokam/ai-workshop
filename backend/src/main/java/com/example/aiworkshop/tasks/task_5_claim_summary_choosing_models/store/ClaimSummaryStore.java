package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.store;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * What the two agents last said about a claim, and what they were looking at when they said it.
 *
 * <p>Both answers are cached against the facts they were derived from, not against the claim: change
 * nothing and reopening the screen costs nothing, change a document and the summary is written
 * again. That is the whole of it, and it matters more here than anywhere else in the application —
 * a claim screen is opened dozens of times a day and these are the two most expensive calls in it.
 *
 * <p>The status note is keyed separately from the summary because it moves for different reasons. A
 * document arriving changes both. A claim handler marking a poor scan as readable changes what the
 * claim is waiting for, and so the status note, while the summary of what the documents say is
 * exactly as it was.
 */
@Component
public class ClaimSummaryStore {

    private final ConcurrentMap<String, Written> summaries = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Written> statusNotes = new ConcurrentHashMap<>();

    public Optional<String> find(String claimId, List<String> documentIds) {
        return lookUp(summaries, claimId, documentIds);
    }

    public void save(String claimId, List<String> documentIds, String summary) {
        summaries.put(claimId, new Written(List.copyOf(documentIds), summary));
    }

    /** The status note, if the facts behind it are the same ones it was written from. */
    public Optional<String> findStatusNote(String claimId, List<String> derivedFrom) {
        return lookUp(statusNotes, claimId, derivedFrom);
    }

    public void saveStatusNote(String claimId, List<String> derivedFrom, String note) {
        statusNotes.put(claimId, new Written(List.copyOf(derivedFrom), note));
    }

    public void deleteAll() {
        summaries.clear();
        statusNotes.clear();
    }

    private static Optional<String> lookUp(ConcurrentMap<String, Written> cache, String claimId, List<String> from) {
        return Optional.ofNullable(cache.get(claimId))
                .filter(written -> written.derivedFrom().equals(from))
                .map(Written::text);
    }

    /** One answer, and the facts it was written from. */
    private record Written(List<String> derivedFrom, String text) {}
}
