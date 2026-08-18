package com.example.aiworkshop.workshop;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns an unfinished task into an answer the screen can explain rather than a stack trace.
 *
 * <p>501 rather than 500: the request was well formed, the server simply does not implement this
 * yet, which is exactly the situation. The body carries the task, the file and the brief, so the
 * screen can point at all three without knowing anything about the exercises.
 *
 * <p>{@link #response} is public because a controller that handles {@code RuntimeException} itself
 * wins over this advice — Spring resolves handlers on the controller first. Those controllers call
 * this rather than writing the shape a second time.
 */
@RestControllerAdvice
public class TaskNotImplementedAdvice {

    public static ResponseEntity<Map<String, Object>> response(TaskNotImplementedException e) {
        WorkshopTask task = e.task();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of(
                        "taskNotImplemented", true,
                        "task", task.number(),
                        "key", task.name(),
                        "title", task.title(),
                        "file", task.file(),
                        "todo", task.todo(),
                        "brief", task.brief()));
    }

    @ExceptionHandler(TaskNotImplementedException.class)
    ResponseEntity<Map<String, Object>> notImplemented(TaskNotImplementedException e) {
        return response(e);
    }
}
