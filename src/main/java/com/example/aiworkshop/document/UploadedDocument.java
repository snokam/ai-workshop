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
 * @param filename the name the browser sent, shown to the user and otherwise not trusted
 * @param contentType the MIME type the browser sent
 * @param sizeBytes size of the uploaded file
 * @param uploadedAt when the upload was accepted
 * @param analysis what the intake agent returned
 */
public record UploadedDocument(
        String id,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        DocumentAnalysis analysis) {}
