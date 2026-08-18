package com.example.aiworkshop.documents.store;

import com.example.aiworkshop.documents.model.UploadedDocument;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentFiles {
    private static final Logger log = LoggerFactory.getLogger(DocumentFiles.class);

    private final Path directory;

    public DocumentFiles(@Value("${aiworkshop.documents.directory}") Path directory) throws IOException {
        this.directory = directory;
        emptyOnStartup();
    }

    public void save(String documentId, byte[] content) {
        try {
            Files.write(directory.resolve(documentId), content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] read(String documentId) {
        Path file = directory.resolve(documentId);
        if (!Files.isRegularFile(file)) {
            throw new MissingFileException(documentId);
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Content contentOf(UploadedDocument document) {
        return contentOf(read(document.id()), document.contentType());
    }

    public static Content contentOf(byte[] content, String mimeType) {
        String base64 = Base64.getEncoder().encodeToString(content);
        return mimeType.equals("application/pdf")
                ? PdfFileContent.from(base64, mimeType)
                : ImageContent.from(base64, mimeType);
    }

    private void emptyOnStartup() throws IOException {
        Files.createDirectories(directory);
        try (Stream<Path> left = Files.walk(directory)) {
            left.filter(path -> !path.equals(directory))
                    .sorted(Comparator.reverseOrder())
                    .forEach(DocumentFiles::delete);
        }
        log.info("Uploaded documents will be kept in {}, emptied at startup", directory.toAbsolutePath());
    }

    private static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class MissingFileException extends RuntimeException {
        MissingFileException(String documentId) {
            super("The file for document " + documentId + " is no longer on disk.");
        }
    }
}
