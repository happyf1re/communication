package com.muravlev.communication.projects;

import com.muravlev.communication.employee.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository; // Чтобы проверить managerUsername

    public ProjectController(ProjectRepository projectRepository,
                             EmployeeRepository employeeRepository) {
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody Project project) {
        // Проверим, что manager существует
        if (!employeeRepository.existsByUsername(project.getManagerUsername())) {
            return ResponseEntity.badRequest()
                    .body("Manager user not found: " + project.getManagerUsername());
        }
        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @Valid @RequestBody Project updated) {
        Optional<Project> opt = projectRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project existing = opt.get();

        // Проверяем наличие manager, если поменялся
        if (!existing.getManagerUsername().equals(updated.getManagerUsername())) {
            if (employeeRepository.existsByUsername(updated.getManagerUsername())) {
                return ResponseEntity.badRequest().body("Manager user not found: " + updated.getManagerUsername());
            }
        }

        existing.setName(updated.getName());
        existing.setManagerUsername(updated.getManagerUsername());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setStatus(updated.getStatus());
        existing.setDescription(updated.getDescription());

        projectRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (!projectRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Дополнительный метод: добавить участника
    @PostMapping("/{projectId}/addParticipant/{employeeId}")
    public ResponseEntity<?> addParticipant(@PathVariable Long projectId,
                                            @PathVariable Long employeeId) {
        var projOpt = projectRepository.findById(projectId);
        if (projOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found: " + employeeId);
        }

        Project project = projOpt.get();
        project.getParticipants().add(empOpt.get());
        projectRepository.save(project);

        return ResponseEntity.ok(project);
    }

    // Удалить участника
    @PostMapping("/{projectId}/removeParticipant/{employeeId}")
    public ResponseEntity<?> removeParticipant(@PathVariable Long projectId,
                                               @PathVariable Long employeeId) {
        var projOpt = projectRepository.findById(projectId);
        if (projOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Project project = projOpt.get();

        var empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found: " + employeeId);
        }

        project.getParticipants().remove(empOpt.get());
        projectRepository.save(project);

        return ResponseEntity.ok(project);
    }
}

