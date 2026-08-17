package com.example.aiworkshop.document;

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

/**
 * The uploaded bytes, kept on disk so an agent can go back and look at the file again. See ADR 0004.
 *
 * <p>Every other store in this application is a map in memory, and this one is deliberately not: a
 * Document's Extraction is what an agent made of a file, and a Case Handler asking "what does the
 * bottom of the scan actually say" is asking a question no Extraction can answer.
 *
 * <p>Two rules make that safe. A file is named by its Document identifier, which is generated here —
 * the filename the browser sent is untrusted text and is never a path. And the directory is emptied
 * on startup, because the records pointing at these files live in memory and do not survive a
 * restart; bytes that outlived them would be unreachable and unattributable.
 */
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

    /**
     * The file as something a model can look at: a PDF as a PDF, a photo as a photo.
     *
     * <p>Nothing is parsed to text on the way, for the same reason intake does not — a question
     * about a scan that is too dark to read cannot be answered from the text a parser managed to
     * pull off it.
     */
    public Content contentOf(UploadedDocument document) {
        return contentOf(read(document.id()), document.contentType());
    }

    static Content contentOf(byte[] content, String mimeType) {
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

    /**
     * Thrown when a Document's file is not where its identifier says it should be.
     *
     * <p>Reached by an agent asking to read a file, so the message goes back as a tool result — the
     * agent's only chance to say so rather than invent what the file said.
     */
    public static class MissingFileException extends RuntimeException {
        MissingFileException(String documentId) {
            super("The file for document " + documentId + " is no longer on disk.");
        }
    }
}
