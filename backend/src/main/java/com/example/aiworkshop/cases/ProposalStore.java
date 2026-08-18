package com.example.aiworkshop.cases;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Where Proposals live. In memory, so everything is lost on restart — the same shape as the other
 * stores, and one more to replace when persistence arrives.
 *
 * <p>Insertion-ordered rather than sorted, because a Proposal has no timestamp worth sorting on: the
 * order that matters is the order the agent raised them in, which is the order they arrived here.
 */
@Component
public class ProposalStore {

    private final Map<String, Proposal> proposals = Collections.synchronizedMap(new LinkedHashMap<>());

    public void save(Proposal proposal) {
        proposals.put(proposal.id(), proposal);
    }

    public Optional<Proposal> findById(String id) {
        return Optional.ofNullable(proposals.get(id));
    }

    /** Every Proposal ever raised on this Case, whatever became of it, oldest first. */
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
