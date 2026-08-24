package com.example.aiworkshop.workshop;

public class TaskNotImplementedException extends RuntimeException {

    /**
     * Throws, but typed, so it can stand where a value the task has not produced yet is expected.
     *
     * <p>It exists so an unwritten step can sit inline in the middle of a method rather than being
     * pushed into a function of its own for the compiler's benefit. The screens catch this and show
     * which file to open.
     */
    public static <T> T notWrittenYet(WorkshopTask task) {
        throw new TaskNotImplementedException(task);
    }

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
