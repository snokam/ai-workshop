package com.example.aiworkshop.workshop;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * The workshop screen tells participants which file to open. If a refactor moves that file and
 * nobody moves the pointer, the first thing a participant does is look for something that is not
 * there — so the pointers are checked here rather than trusted.
 */
class WorkshopTaskTest {
    private static final Path REPO = Path.of("..");

    @Test
    void everyTaskPointsAtAFileThatExists() {
        for (WorkshopTask task : WorkshopTask.values()) {
            assertThat(REPO.resolve(task.file()))
                    .as("task %d (%s) points at %s", task.number(), task.title(), task.file())
                    .exists();
        }
    }

    @Test
    void everyTaskPointsAtABriefThatExists() {
        for (WorkshopTask task : WorkshopTask.values()) {
            assertThat(REPO.resolve(task.brief()))
                    .as("task %d (%s) has a brief at %s", task.number(), task.title(), task.brief())
                    .exists();
        }
    }

    @Test
    void everyTaskSaysWhatToDoInAFinishedSentence() {
        for (WorkshopTask task : WorkshopTask.values()) {
            assertThat(task.todo())
                    .as("task %d (%s) is the first thing a participant reads", task.number(), task.title())
                    .isNotBlank()
                    .endsWith(".");
        }
    }

    @Test
    void theTasksAreNumberedInOrderWithNoGaps() {
        int expected = 1;
        for (WorkshopTask task : WorkshopTask.values()) {
            assertThat(task.number()).isEqualTo(expected++);
        }
    }

    @Test
    void everyTaskFolderIsNamedForItsNumber() throws Exception {
        try (var tasks = Files.list(REPO.resolve("backend/src/main/java/com/example/aiworkshop/tasks"))) {
            assertThat(tasks.filter(Files::isDirectory).map(p -> p.getFileName().toString()))
                    .allMatch(name -> name.matches("task_[1-8]_\\w+"));
        }
    }
}
