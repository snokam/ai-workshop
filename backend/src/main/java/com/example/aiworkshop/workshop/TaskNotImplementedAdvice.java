package com.example.aiworkshop.workshop;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
