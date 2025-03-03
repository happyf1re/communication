package com.muravlev.communication.projects;

import com.muravlev.communication.employee.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;

    public TaskController(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestParam Long projectId, @Valid @RequestBody Task task) {
        // Проверяем проект
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Project not found: " + projectId);
        }
        // Проверяем assignee
        if (!employeeRepository.existsByUsername(task.getAssigneeUsername())) {
            return ResponseEntity.badRequest().body("Assignee user not found: " + task.getAssigneeUsername());
        }

        task.setProject(projectOpt.get());
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    // Список всех задач
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Задача по ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return taskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @Valid @RequestBody Task updated) {
        Optional<Task> opt = taskRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Task existing = opt.get();

        // Если поменяли assigneeUsername
        if (!existing.getAssigneeUsername().equals(updated.getAssigneeUsername())) {
            if (!employeeRepository.existsByUsername(updated.getAssigneeUsername())) {
                return ResponseEntity.badRequest().body("Assignee user not found: " + updated.getAssigneeUsername());
            }
        }

        existing.setTitle(updated.getTitle());
        existing.setAssigneeUsername(updated.getAssigneeUsername());
        existing.setDueDate(updated.getDueDate());
        existing.setStatus(updated.getStatus());
        existing.setDescription(updated.getDescription());
        // timeSpentMinutes - можем либо пересчитывать, либо брать из updated
        existing.setTimeSpentMinutes(updated.getTimeSpentMinutes());

        // сейчас считаем, что проект не меняется

        taskRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // Удалить
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // По проекту
    @GetMapping("/byProject/{projectId}")
    public List<Task> getTasksByProject(@PathVariable Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // По сотруднику
    @GetMapping("/byAssignee/{username}")
    public List<Task> getTasksByAssignee(@PathVariable String username) {
        return taskRepository.findByAssigneeUsername(username);
    }
}
