package com.muravlev.communication.hr;

import com.muravlev.communication.employee.Employee;
import com.muravlev.communication.employee.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    public TeamController(TeamRepository teamRepository, EmployeeRepository employeeRepository) {
        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
    }

    // Создать команду
    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody Team team) {
        // Проверяем managerUsername, если не пуст, существует ли такой Employee
        if (team.getManagerUsername() != null && !team.getManagerUsername().isBlank()) {
            if (!employeeRepository.existsByUsername(team.getManagerUsername())) {
                return ResponseEntity.badRequest().body("Manager user not found: " + team.getManagerUsername());
            }
        }
        Team saved = teamRepository.save(team);
        return ResponseEntity.ok(saved);
    }

    // Список всех команд
    @GetMapping
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    // Получить одну команду
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeam(@PathVariable Long id) {
        return teamRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить команду
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @Valid @RequestBody Team updated) {
        Optional<Team> opt = teamRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Team existing = opt.get();

        // Проверка manager
        if (updated.getManagerUsername() != null && !updated.getManagerUsername().isBlank()) {
            if (!employeeRepository.existsByUsername(updated.getManagerUsername())) {
                return ResponseEntity.badRequest().body("Manager user not found: " + updated.getManagerUsername());
            }
        }

        existing.setName(updated.getName());
        existing.setManagerUsername(updated.getManagerUsername());
        // members можно либо заменять целиком, либо отдельно добавлять/удалять
        existing.setMembers(updated.getMembers());

        teamRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // Удалить команду
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        if (!teamRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        teamRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Пример метода: Добавить участника
    @PostMapping("/{id}/addMember/{employeeId}")
    public ResponseEntity<?> addMember(@PathVariable Long id, @PathVariable Long employeeId) {
        var teamOpt = teamRepository.findById(id);
        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found: " + employeeId);
        }
        Team team = teamOpt.get();
        Employee emp = empOpt.get();

        team.getMembers().add(emp);
        teamRepository.save(team);
        return ResponseEntity.ok(team);
    }

    // Удалить участника
    @PostMapping("/{id}/removeMember/{employeeId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id, @PathVariable Long employeeId) {
        var teamOpt = teamRepository.findById(id);
        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Team team = teamOpt.get();

        // team.members.removeIf(e -> e.getId().equals(employeeId));
        // Или находим конкретного emp:
        var empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Employee not found: " + employeeId);
        }
        team.getMembers().remove(empOpt.get());
        teamRepository.save(team);
        return ResponseEntity.ok(team);
    }
}

