package com.muravlev.communication.projects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public ReportsController(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    // Просроченные задачи (status != DONE, dueDate < today)
    @GetMapping("/overdueTasks")
    public List<Task> getOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll().stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getStatus() != TaskStatus.DONE)
                .collect(Collectors.toList());
    }

    // Список проектов, у которых endDate < today + 7 и status != COMPLETED
    @GetMapping("/endingSoonProjects")
    public List<Project> getEndingSoonProjects() {
        LocalDate threshold = LocalDate.now().plusDays(7);
        return projectRepository.findAll().stream()
                .filter(p -> p.getEndDate() != null
                        && p.getEndDate().isBefore(threshold)
                        && p.getStatus() != ProjectStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    // Количество задач на каждого исполнителя
    @GetMapping("/tasksByAssigneeCount")
    public List<AssigneeTaskCountDTO> getTasksByAssigneeCount() {
        // Можем сделать через Stream grouping
        // Или через кастомный JPQL
        return taskRepository.findAll().stream()
                .collect(Collectors.groupingBy(Task::getAssigneeUsername, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new AssigneeTaskCountDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    // DTO для возврата {assignee, count}
    static record AssigneeTaskCountDTO(String assigneeUsername, Long count) { }
}
