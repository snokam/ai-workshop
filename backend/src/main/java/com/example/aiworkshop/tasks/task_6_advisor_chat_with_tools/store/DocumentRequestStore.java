package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.store;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals.DocumentRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DocumentRequestStore {
    private final Map<String, DocumentRequest> requests = Collections.synchronizedMap(new LinkedHashMap<>());

    public void save(DocumentRequest request) {
        requests.put(request.id(), request);
    }

    public List<DocumentRequest> findByClaimId(String claimId) {
        synchronized (requests) {
            return requests.values().stream()
                    .filter(request -> request.claimId().equals(claimId))
                    .toList();
        }
    }

    public void deleteAll() {
        requests.clear();
    }
}
