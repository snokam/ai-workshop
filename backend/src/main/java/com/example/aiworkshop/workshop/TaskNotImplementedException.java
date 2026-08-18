package com.example.aiworkshop.workshop;

/**
 * Thrown when a feature is used before the task that provides it has been written.
 *
 * <p>Carries the task rather than a message, so every layer above — the error handler, the API
 * response, the screen — can say the same thing without any of them hard-coding it.
 */
public class TaskNotImplementedException extends RuntimeException {

    private final transient WorkshopTask task;

    public TaskNotImplementedException(WorkshopTask task) {
        super("Task %d (%s) is not implemented yet. Open %s — %s"
                .formatted(task.number(), task.title(), task.file(), task.todo()));
        this.task = task;
    }

    public WorkshopTask task() {
        return task;
    }
}
