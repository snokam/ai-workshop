package com.example.aiworkshop.tasks.task_3_document_agent.progress;

import com.example.aiworkshop.tasks.task_1_first_agent.ClaimProgress;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Task 3's answer to task 1's question, which replaces task 1's own.
 *
 * <p>Both beans exist; this one is {@code @Primary}, so from the moment task 3 is on the classpath
 * the claim list stops saying "awaiting documents" for everything and starts reflecting what was
 * actually uploaded. Nothing in task 1 changed, and nothing in task 1 knows this class exists.
 */
@Service
@Primary
public class DocumentProgress implements ClaimProgress {
    private final DocumentStore documents;

    public DocumentProgress(DocumentStore documents) {
        this.documents = documents;
    }

    @Override
    public ClaimStatus statusOf(Claim theClaim) {
        return ClaimDocuments.statusOf(theClaim, documents.findByClaimId(theClaim.id()));
    }

    @Override
    public List<String> outstandingFor(Claim theClaim) {
        return ClaimDocuments.unmatchedRequiredDocuments(theClaim, documents.findByClaimId(theClaim.id()));
    }
}
