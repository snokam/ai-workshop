package com.example.aiworkshop.fraud;

import com.example.aiworkshop.document.DocumentAnalysis;

public record ScreenedFile(
        String documentId,
        String caseId,
        String filename,
        String contentType,
        byte[] content,
        DocumentAnalysis analysis) {
    public boolean isImage() {
        return contentType.startsWith("image/");
    }

    public boolean isJpeg() {
        return contentType.equals("image/jpeg");
    }
}
