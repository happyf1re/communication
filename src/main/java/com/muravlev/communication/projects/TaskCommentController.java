package com.muravlev.communication.projects;

import com.muravlev.communication.employee.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class TaskCommentController {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public TaskCommentController(TaskCommentRepository commentRepository,
                                 TaskRepository taskRepository, EmployeeRepository employeeRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
    }

    // Создать комментарий
    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long taskId,
                                        @Valid @RequestBody TaskComment comment) {
        // Проверим, есть ли задача
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Task not found: " + taskId);
        }

        // Проверим, есть ли сотрудник
        if (!employeeRepository.existsByUsername(comment.getAuthorUsername())) {
            return ResponseEntity.badRequest().body("User not found: " + comment.getAuthorUsername());
        }

        // Сохраняем
        comment.setTask(taskOpt.get());
        comment.setCreatedAt(LocalDateTime.now());
        TaskComment saved = commentRepository.save(comment);
        return ResponseEntity.ok(saved);
    }

    // Список комментариев задачи
    @GetMapping
    public ResponseEntity<List<TaskComment>> getComments(@PathVariable Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.badRequest().build(); // или 404
        }
        List<TaskComment> comments = commentRepository.findByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }

    // Получить 1 комментарий
    @GetMapping("/{commentId}")
    public ResponseEntity<TaskComment> getComment(@PathVariable Long taskId,
                                                  @PathVariable Long commentId) {
        // Тут тоже можно проверять taskId
        return commentRepository.findById(commentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Удалить комментарий
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long taskId,
                                              @PathVariable Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            return ResponseEntity.notFound().build();
        }
        commentRepository.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }
}

