package com.example.aiworkshop.tasks.task_3_document_agent.store;

import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;

/**
 * What kind of file this is: from the browser if it said, and from the name if it did not.
 *
 * <p>Plumbing, and deliberately out of the way of the task. It is here rather than on intake because
 * the answer is only ever used by {@link DocumentFiles#contentOf}, which turns the same mime type
 * into the PDF or image content the model is sent — so the two halves of "what is this file" now sit
 * in the same folder.
 *
 * <p>One thing in it is not plumbing. Nothing here converts anything: the type is worked out and the
 * bytes are passed on untouched, including HEIC, which no browser will draw. That is the whole point
 * of the task — the model is handed the document, not something extracted from it.
 */
public final class FileType {

    private FileType() {}

    public static String of(MultipartFile file) {
        String declared = file.getContentType();
        if (declared != null && (declared.equals("application/pdf") || declared.startsWith("image/"))) {
            return declared;
        }
        return fromTheName(file.getOriginalFilename(), declared);
    }

    private static String fromTheName(String filename, String declared) {
        String name = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        return switch (name.substring(name.lastIndexOf('.') + 1)) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            // Every photo taken on an iPhone since 2017. Browsers vary on whether they declare a
            // type for it, so the extension has to be enough — and the model reads it as it is.
            case "heic", "heif" -> "image/heic";
            default -> throw new UnsupportedDocumentException(
                    "Only PDFs and images can be analysed. Received: " + declared);
        };
    }

    public static class UnsupportedDocumentException extends RuntimeException {
        UnsupportedDocumentException(String message) {
            super(message);
        }
    }
}
