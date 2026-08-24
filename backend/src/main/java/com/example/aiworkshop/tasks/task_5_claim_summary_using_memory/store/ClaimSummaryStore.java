package com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.store;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class ClaimSummaryStore {
    private final ConcurrentMap<String, WrittenSummary> summaries = new ConcurrentHashMap<>();

    public Optional<String> find(String claimId, List<String> documentIds) {
        return Optional.ofNullable(summaries.get(claimId))
                .filter(written -> written.documentIds().equals(documentIds))
                .map(WrittenSummary::summary);
    }

    public void save(String claimId, List<String> documentIds, String summary) {
        summaries.put(claimId, new WrittenSummary(List.copyOf(documentIds), summary));
    }

    public void deleteAll() {
        summaries.clear();
    }

    private record WrittenSummary(List<String> documentIds, String summary) {}
}
