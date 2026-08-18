package com.example.aiworkshop.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentFilesTest {
    private static final byte[] SCAN = "%PDF-1.4".getBytes();

    @TempDir
    Path directory;

    @Test
    void aFileIsWrittenUnderItsDocumentIdentifier() throws IOException {
        DocumentFiles files = new DocumentFiles(directory);

        files.save("d-1", SCAN);

        assertThat(directory.resolve("d-1")).exists();
        assertThat(files.read("d-1")).isEqualTo(SCAN);
    }

    @Test
    void whatTheLastRunLeftBehindIsGoneOnStartup() throws IOException {
        Files.write(directory.resolve("from-the-last-run"), SCAN);

        new DocumentFiles(directory);

        assertThat(directory).isEmptyDirectory();
    }

    @Test
    void aFileThatIsNotThereSaysSo() throws IOException {
        DocumentFiles files = new DocumentFiles(directory);

        assertThatThrownBy(() -> files.read("d-1"))
                .isInstanceOf(DocumentFiles.MissingFileException.class)
                .hasMessageContaining("d-1");
    }
}
