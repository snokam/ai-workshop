package com.example.aiworkshop.tasks.task_5_claim_summary.store;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class CaseSummaryStore {
    private final ConcurrentMap<String, WrittenSummary> summaries = new ConcurrentHashMap<>();

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
