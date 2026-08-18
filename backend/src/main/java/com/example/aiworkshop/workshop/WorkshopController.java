package com.example.aiworkshop.workshop;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * What the screens read to find out how far the workshop has got.
 *
 * <p>Without this a screen only learns a task is unfinished by trying it and catching the failure,
 * which means the explanation always arrives after a button press. Reading the state up front lets
 * a screen say what is missing before anyone clicks, and disable what cannot work yet.
 */
@RestController
@RequestMapping("/api/workshop")
class WorkshopController {

    private final TaskProgress progress;

    WorkshopController(TaskProgress progress) {
        this.progress = progress;
    }

    @GetMapping("/tasks")
    List<TaskState> tasks() {
        return Arrays.stream(WorkshopTask.values())
                .map(task -> new TaskState(
                        task.number(),
                        task.name(),
                        task.title(),
                        task.file(),
                        task.todo(),
                        task.brief(),
                        progress.isDone(task)))
                .toList();
    }

    /**
     * One exercise, as a screen sees it.
     *
     * @param number the order it is done in
     * @param key the stable identifier a screen matches on, so renumbering does not break the UI
     * @param title what the brief calls it
     * @param file the file to open, from the repository root
     * @param todo one sentence on what to write there
     * @param brief where the exercise is described
     * @param done whether the agent behind it has been written
     */
    record TaskState(
            int number, String key, String title, String file, String todo, String brief, boolean done) {}
}
