package com.example.aiworkshop.fraud;

import com.example.aiworkshop.document.DocumentAnalysis;

/**
 * What a {@link FraudCheck} is given: the file as it arrived, plus what the intake agent made of it.
 *
 * <p>The bytes are here and nowhere else. {@code UploadedDocument} deliberately does not keep them,
 * so screening happens at intake, while the upload is still in hand, or it does not happen at all.
 * That is also why a Screening is never recomputed later.
 *
 * @param documentId the Document about to be stored
 * @param caseId the Case it is going into — a check may care whether the same bytes turned up in a
 *     different one
 * @param filename the name the browser sent. Not trusted, but a check may still find it telling
 * @param contentType the resolved MIME type, already reconciled with the extension by intake
 * @param content the uploaded bytes
 * @param analysis what the agent returned, so a check can act on something the model noticed
 *     without a second model call
 */
public record ScreenedFile(
        String documentId,
        String caseId,
        String filename,
        String contentType,
        byte[] content,
        DocumentAnalysis analysis) {

    /** Several checks only make sense on a photo; a PDF has no EXIF and no reverse image search. */
    public boolean isImage() {
        return contentType.startsWith("image/");
    }

    public boolean isJpeg() {
        return contentType.equals("image/jpeg");
    }
}
