package com.example.aiworkshop.workshop;

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
