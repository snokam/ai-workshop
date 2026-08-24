package com.example.aiworkshop.tasks.task_3_document_agent.progress;

import com.example.aiworkshop.tasks.task_1_first_agent.CaseProgress;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Task 3's answer to task 1's question, which replaces task 1's own.
 *
 * <p>Both beans exist; this one is {@code @Primary}, so from the moment task 3 is on the classpath
 * the case list stops saying "awaiting documents" for everything and starts reflecting what was
 * actually uploaded. Nothing in task 1 changed, and nothing in task 1 knows this class exists.
 */
@Service
@Primary
public class DocumentProgress implements CaseProgress {
    private final DocumentStore documents;

    public DocumentProgress(DocumentStore documents) {
        this.documents = documents;
    }

    @Override
    public CaseStatus statusOf(Case theCase) {
        return CaseDocuments.statusOf(theCase, documents.findByCaseId(theCase.id()));
    }

    @Override
    public List<String> outstandingFor(Case theCase) {
        return CaseDocuments.unmatchedRequiredDocuments(theCase, documents.findByCaseId(theCase.id()));
    }
}
