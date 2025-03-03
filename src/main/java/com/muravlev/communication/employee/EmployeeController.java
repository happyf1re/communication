package com.muravlev.communication.employee;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Создать сотрудника
    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee) {
        // Проверка: username уникален?
        if (employeeRepository.existsByUsername(employee.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already taken: " + employee.getUsername());
        }
        Employee saved = employeeRepository.save(employee);
        return ResponseEntity.ok(saved);
    }

    // Получить список
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Получить по ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Получить по username
    @GetMapping("/byUsername/{username}")
    public ResponseEntity<Employee> getByUsername(@PathVariable String username) {
        return employeeRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,
                                            @Valid @RequestBody Employee updated) {
        var opt = employeeRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Employee existing = opt.get();

        // Если пользователь меняет username, проверим уникальность
        if (!existing.getUsername().equals(updated.getUsername())) {
            if (employeeRepository.existsByUsername(updated.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Username already taken: " + updated.getUsername());
            }
        }

        existing.setUsername(updated.getUsername());
        existing.setPassword(updated.getPassword());
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDepartment(updated.getDepartment());
        existing.setRole(updated.getRole());

        employeeRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // Удалить
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        employeeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

