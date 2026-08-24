package com.example.aiworkshop.tasks;

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
 * The rule the whole layout rests on: a task may use the tasks before it and must not know about
 * the tasks after it.
 *
 * <p>It is what makes the workshop work in order. A participant who has written tasks 1 and 2 has a
 * program that compiles and runs, because nothing in those two folders refers to anything in the
 * six that follow. Break the rule and task 2 stops making sense on its own — which is the kind of
 * thing that is obvious in a review and invisible a month later, so it is checked here.
 *
 * <p>When a later task needs to change how an earlier one behaves, it contributes rather than being
 * called: task 2 answers task 1's ClaimProgress, task 3 hands its guardrails to task 2 as beans,
 * task 4 listens for task 2's event. The dependency still points backwards in every claim.
 */
class TaskDependencyTest {
    private static final Path TASKS = Path.of("src/main/java/com/example/aiworkshop/tasks");
    private static final Pattern IMPORT = Pattern.compile("^import (?:static )?[\\w.]*tasks\\.task_(\\d)_", Pattern.MULTILINE);
    private static final Pattern OWNER = Pattern.compile("task_(\\d)_");

    @Test
    void noTaskDependsOnATaskThatComesAfterIt() throws Exception {
        List<String> forwards = new ArrayList<>();
        try (Stream<Path> sources = Files.walk(TASKS)) {
            for (Path source : sources.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher owner = OWNER.matcher(source.toString());
                owner.find();
                int me = Integer.parseInt(owner.group(1));
                Matcher used = IMPORT.matcher(Files.readString(source));
                while (used.find()) {
                    int them = Integer.parseInt(used.group(1));
                    if (them > me) {
                        forwards.add("task %d needs task %d: %s".formatted(me, them, source.getFileName()));
                    }
                }
            }
        }
        assertThat(forwards)
                .as("a task may only depend on tasks before it, so the workshop can be done in order")
                .isEmpty();
    }
}
