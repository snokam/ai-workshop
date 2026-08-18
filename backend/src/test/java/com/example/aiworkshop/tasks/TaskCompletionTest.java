package com.example.aiworkshop.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.workshop.TaskProgress;
import com.example.aiworkshop.workshop.WorkshopTask;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * One test per exercise, red until it is written. This is the workshop's progress bar: run it and
 * the failures are what is left, each naming the file to open.
 *
 * <p>It asks {@link TaskProgress}, which is what the screens ask, so the test and the application
 * can never disagree about what is done. Nothing here calls a model — what an agent writes back
 * depends on one, and a test that waited for that would be slow, need credentials, and fail for
 * reasons that have nothing to do with the exercise. The tests that check behaviour sit in each
 * task's own folder.
 */
@SpringBootTest
class TaskCompletionTest {

    @Autowired
    private TaskProgress progress;

    static Stream<WorkshopTask> tasks() {
        return Stream.of(WorkshopTask.values());
    }

    @DisplayName("task is implemented")
    @ParameterizedTest(name = "task {0}")
    @MethodSource("tasks")
    void isImplemented(WorkshopTask task) {
        assertThat(progress.isDone(task))
                .describedAs(
                        """

                        Task %d — %s is not written yet.

                          Open   %s
                          Brief  %s
                          To do  %s
                        """
                                .formatted(task.number(), task.title(), task.file(), task.brief(), task.todo()))
                .isTrue();
    }
}
