package com.example.aiworkshop.cases.proposals;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ProposalStore {
    private final Map<String, Proposal> proposals = Collections.synchronizedMap(new LinkedHashMap<>());

    public void save(Proposal proposal) {
        proposals.put(proposal.id(), proposal);
    }

    public Optional<Proposal> findById(String id) {
        return Optional.ofNullable(proposals.get(id));
    }

    public List<Proposal> findByCaseId(String caseId) {
        synchronized (proposals) {
            return proposals.values().stream()
                    .filter(proposal -> proposal.caseId().equals(caseId))
                    .toList();
        }
    }

    public void deleteAll() {
        proposals.clear();
    }
}
