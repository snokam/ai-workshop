package com.example.aiworkshop.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.workshop.WorkshopTask;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * One test per exercise, red until it is written.
 *
 * <p>This is the workshop's progress bar. Run it and the failures are the tasks still to do, each
 * naming the file to open. It asserts the flag rather than the behaviour on purpose: what an agent
 * writes back depends on a model, so a test that called one would be slow, need credentials, and
 * fail for reasons that have nothing to do with whether the exercise was done. The tests that
 * check what the code actually does sit in the task's own folder next to it.
 */
class TaskCompletionTest {

    static Stream<WorkshopTask> tasks() {
        return Stream.of(WorkshopTask.values());
    }

    @DisplayName("task is implemented")
    @ParameterizedTest(name = "task {0}")
    @MethodSource("tasks")
    void isImplemented(WorkshopTask task) {
        assertThat(TaskFlags.isDone(task))
                .describedAs(
                        """

                        Task %d — %s is not implemented yet.

                          Open   %s
                          Brief  %s
                          To do  %s

                        Set IMPLEMENTED to true in that file once you have written it.
                        """
                                .formatted(task.number(), task.title(), task.file(), task.brief(), task.todo()))
                .isTrue();
    }
}
