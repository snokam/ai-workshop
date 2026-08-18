package com.example.aiworkshop.workshop;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    record TaskState(
            int number, String key, String title, String file, String todo, String brief, boolean done) {}
}
