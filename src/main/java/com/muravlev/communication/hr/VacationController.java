package com.muravlev.communication.hr;

import com.muravlev.communication.employee.Employee;
import com.muravlev.communication.employee.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vacations")
public class VacationController {

    private final VacationRepository vacationRepository;
    private final EmployeeRepository employeeRepository;

    public VacationController(VacationRepository vacationRepository,
                              EmployeeRepository employeeRepository) {
        this.vacationRepository = vacationRepository;
        this.employeeRepository = employeeRepository;
    }

    // Создать отпуск
    @PostMapping
    public ResponseEntity<?> createVacation(@Valid @RequestBody Vacation vacation) {
        // Проверка, что employee не null
        if (vacation.getEmployee() == null) {
            return ResponseEntity.badRequest().body("No employee specified");
        }

        // Проверка, что employee.id существует
        Long empId = vacation.getEmployee().getId();
        if (!employeeRepository.existsById(empId)) {
            return ResponseEntity.badRequest().body("Employee not found: " + empId);
        }

        // Валидация дат: startDate <= endDate?
        if (vacation.getStartDate().isAfter(vacation.getEndDate())) {
            return ResponseEntity.badRequest().body("startDate cannot be after endDate");
        }

        Vacation saved = vacationRepository.save(vacation);
        return ResponseEntity.ok(saved);
    }

    // Список всех отпусков
    @GetMapping
    public List<Vacation> getAllVacations() {
        return vacationRepository.findAll();
    }

    // Получить отпуск по id
    @GetMapping("/{id}")
    public ResponseEntity<Vacation> getVacation(@PathVariable Long id) {
        return vacationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Обновить
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVacation(@PathVariable Long id, @Valid @RequestBody Vacation updated) {
        var opt = vacationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Vacation existing = opt.get();

        // Проверка employee
        if (updated.getEmployee() == null || updated.getEmployee().getId() == null) {
            return ResponseEntity.badRequest().body("Employee required");
        }
        if (!employeeRepository.existsById(updated.getEmployee().getId())) {
            return ResponseEntity.badRequest().body("Employee not found: " + updated.getEmployee().getId());
        }

        // Проверка дат
        if (updated.getStartDate().isAfter(updated.getEndDate())) {
            return ResponseEntity.badRequest().body("startDate cannot be after endDate");
        }

        existing.setEmployee(updated.getEmployee());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setStatus(updated.getStatus());
        existing.setComment(updated.getComment());

        vacationRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    // Удалить отпуск
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacation(@PathVariable Long id) {
        if (!vacationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vacationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Возвращает все отпуска заданного сотрудника
    @GetMapping("/byEmployee/{employeeId}")
    public ResponseEntity<List<Vacation>> getVacationsByEmployee(@PathVariable Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            return ResponseEntity.badRequest().build();
        }
        List<Vacation> list = vacationRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(list);
    }
}

