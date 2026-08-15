package com.example.aiworkshop.document;

import java.time.Instant;

/**
 * A file someone uploaded, together with what the intake agent made of it.
 *
 * <p>The bytes are not kept. The browser already holds the file it just sent, so it can render its
 * own preview; storing a second copy server-side would buy nothing until documents have to outlive
 * the page that uploaded them.
 *
 * @param id opaque identifier, generated on upload
 * @param caseId the Case this Document belongs to. Never absent — a Document uploaded into nothing
 *     is work the agent does that nobody can use
 * @param filename the name the browser sent, shown to the user and otherwise not trusted
 * @param contentType the MIME type the browser sent
 * @param sizeBytes size of the uploaded file
 * @param uploadedAt when the upload was accepted
 * @param analysis what the intake agent returned
 * @param reviewed whether a Case Handler has confirmed this Document is good enough to work with
 *     despite its Quality Assessment. Only ever true because a human said so
 */
public record UploadedDocument(
        String id,
        String caseId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        DocumentAnalysis analysis,
        boolean reviewed) {

    /** A Case Handler's judgement, which beats the agent's when they can read the file and it could not. */
    public UploadedDocument markReviewed() {
        return new UploadedDocument(id, caseId, filename, contentType, sizeBytes, uploadedAt, analysis, true);
    }
}
