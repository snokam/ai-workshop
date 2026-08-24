package com.example.aiworkshop.tasks.task_3_document_agent;

import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;

/**
 * A document has been read and kept. Published after the upload is safely stored, never before.
 *
 * <p>Intake announces this and does not care who listens. That ordering is the rule task 4 turns
 * out to be about: screening happens to a document that already exists, so no check can stand
 * between someone and their own case, and a check that fails takes nothing down with it.
 */
public record DocumentStored(
        String documentId,
        String caseId,
        String filename,
        String contentType,
        byte[] content,
        String contentHash,
        DocumentAnalysis analysis) {}
