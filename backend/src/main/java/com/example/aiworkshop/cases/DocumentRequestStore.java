package com.example.aiworkshop.cases;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Where Document Requests live. In memory, so everything is lost on restart — the same shape as the
 * other stores.
 *
 * <p>Insertion-ordered, for the same reason {@link ProposalStore} is: the order a Claimant should
 * read them in is the order they were asked.
 */
@Component
public class DocumentRequestStore {

    private final Map<String, DocumentRequest> requests = Collections.synchronizedMap(new LinkedHashMap<>());

    public void save(DocumentRequest request) {
        requests.put(request.id(), request);
    }

    public List<DocumentRequest> findByCaseId(String caseId) {
        synchronized (requests) {
            return requests.values().stream()
                    .filter(request -> request.caseId().equals(caseId))
                    .toList();
        }
    }

    public void deleteAll() {
        requests.clear();
    }
}
