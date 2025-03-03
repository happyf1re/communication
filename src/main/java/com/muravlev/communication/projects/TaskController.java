package com.muravlev.communication.projects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskController(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestParam Long projectId, @RequestBody Task task) {
        Optional<Project> opt = projectRepository.findById(projectId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Project not found: " + projectId);
        }
        task.setProject(opt.get());
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task updated) {
        Optional<Task> opt = taskRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Task existing = opt.get();
        existing.setTitle(updated.getTitle());
        existing.setAssigneeUsername(updated.getAssigneeUsername());
        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setDescription(updated.getDescription());
        taskRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Задачи конкретного проекта
    @GetMapping("/byProject/{projectId}")
    public ResponseEntity<List<Task>> tasksByProject(@PathVariable Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    // Задачи конкретного сотрудника (по username)
    @GetMapping("/byAssignee/{username}")
    public ResponseEntity<List<Task>> tasksByAssignee(@PathVariable String username) {
        List<Task> tasks = taskRepository.findByAssigneeUsername(username);
        return ResponseEntity.ok(tasks);
    }
}
