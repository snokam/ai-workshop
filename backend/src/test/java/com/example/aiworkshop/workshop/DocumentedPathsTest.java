package com.example.aiworkshop.workshop;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Every file the written material points at has to be there.
 *
 * <p>The briefs send people to a specific file to write a specific thing, so a path that has moved
 * costs a participant the worst ten minutes of the workshop: they cannot tell whether they have
 * misread the instruction or the instruction is wrong. Cheaper to fail here.
 */
class DocumentedPathsTest {
    private static final Path REPO = Path.of("..");
    /**
     * Two ways a document points at a file: in backticks as prose, and as the target of a markdown
     * link. Only the first was checked here, which is how [task 2](./tasks/task_2_postprocessing.md)
     * survived two renumberings — the file it named had not existed for months and nothing said so.
     */
    private static final Pattern PATH =
            Pattern.compile("`([\\w@/.-]+\\.(?:java|tsx?|md))`|\\]\\(([\\w@/.-]+\\.(?:java|tsx?|md))[)#]");

    /**
     * A path in the briefs is written from wherever makes it readable: from the repository root,
     * from the Java source root, from the tasks folder, or from the folder the document is in. All
     * four are fair, so a path counts as found if it resolves under any of them.
     */
    private static final List<String> ROOTS = List.of(
            "", "backend/src/main/java/com/example/aiworkshop/",
            "backend/src/main/java/com/example/aiworkshop/tasks/", "frontend/src/");

    @Test
    void everyPathNamedInTheWrittenMaterialExists() throws Exception {
        List<String> missing = new ArrayList<>();
        try (Stream<Path> docs = Files.walk(REPO)) {
            for (Path doc : docs.filter(DocumentedPathsTest::isOurMarkdown).toList()) {
                Matcher named = PATH.matcher(Files.readString(doc));
                while (named.find()) {
                    String path = named.group(1) != null ? named.group(1) : named.group(2);
                    if (!path.contains("/")) {
                        continue; // a bare filename is prose, not a pointer
                    }
                    if (!resolvesSomewhere(doc, path)) {
                        missing.add("%s points at %s".formatted(REPO.relativize(doc), path));
                    }
                }
            }
        }
        assertThat(missing).as("the briefs must point at files that exist").isEmpty();
    }

    private static boolean resolvesSomewhere(Path doc, String path) {
        if (Files.exists(doc.getParent().resolve(path))) {
            return true;
        }
        return ROOTS.stream().anyMatch(root -> Files.exists(REPO.resolve(root + path)));
    }

    private static boolean isOurMarkdown(Path path) {
        String name = path.toString();
        return name.endsWith(".md") && !name.contains("node_modules") && !name.contains("/target/");
    }
}
