package com.example.aiworkshop.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The one place bytes outlive a request. See ADR 0003 for why they are kept at all. */
class DocumentFilesTest {

    private static final byte[] SCAN = "%PDF-1.4".getBytes();

    @TempDir
    Path directory;

    /**
     * The filename the browser sent is untrusted text, and a Claimant who could choose it could
     * choose a path. The Document identifier is generated here and cannot be one.
     */
    @Test
    void aFileIsWrittenUnderItsDocumentIdentifier() throws IOException {
        DocumentFiles files = new DocumentFiles(directory);

        files.save("d-1", SCAN);

        assertThat(directory.resolve("d-1")).exists();
        assertThat(files.read("d-1")).isEqualTo(SCAN);
    }

    /**
     * The records that point at these files are in memory and do not survive a restart. Bytes that
     * did would be unreachable and unattributable — worse than losing both together.
     */
    @Test
    void whatTheLastRunLeftBehindIsGoneOnStartup() throws IOException {
        Files.write(directory.resolve("from-the-last-run"), SCAN);

        new DocumentFiles(directory);

        assertThat(directory).isEmptyDirectory();
    }

    /** The Document record has no path component, so a missing file is found here or not at all. */
    @Test
    void aFileThatIsNotThereSaysSo() throws IOException {
        DocumentFiles files = new DocumentFiles(directory);

        assertThatThrownBy(() -> files.read("d-1"))
                .isInstanceOf(DocumentFiles.MissingFileException.class)
                .hasMessageContaining("d-1");
    }
}
