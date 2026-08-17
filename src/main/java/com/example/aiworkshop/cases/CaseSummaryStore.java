package com.example.aiworkshop.cases;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Where a written Case Summary is kept between page views. In memory, so everything is lost on
 * restart — the third of the stores to replace when persistence arrives.
 *
 * <p>A summary is held against the Documents it was written over rather than against the Case
 * alone. That is what makes it go stale by itself: a new upload changes the set, so the next read
 * misses and the agent runs again, and nothing anywhere has to remember to invalidate it. A Review
 * changes no Document's identity, so the summary survives one — which is right, since a Review does
 * not change a word of what the Documents say.
 */
@Component
public class CaseSummaryStore {

    private final ConcurrentMap<String, WrittenSummary> summaries = new ConcurrentHashMap<>();

    /** The summary written over exactly these Documents, or empty if the set has moved on. */
    public Optional<String> find(String caseId, List<String> documentIds) {
        return Optional.ofNullable(summaries.get(caseId))
                .filter(written -> written.documentIds().equals(documentIds))
                .map(WrittenSummary::summary);
    }

    public void save(String caseId, List<String> documentIds, String summary) {
        summaries.put(caseId, new WrittenSummary(List.copyOf(documentIds), summary));
    }

    public void deleteAll() {
        summaries.clear();
    }

    private record WrittenSummary(List<String> documentIds, String summary) {}
}
