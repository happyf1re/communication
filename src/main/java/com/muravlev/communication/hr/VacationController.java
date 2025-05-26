package com.muravlev.communication.hr;

import com.muravlev.communication.employee.Employee;
import com.muravlev.communication.employee.EmployeeRepository;
import com.muravlev.communication.employee.EmployeeRole;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
public class VacationController {

    private final VacationRepository vacationRepository;
    private final EmployeeRepository employeeRepository;

    /* ---------- обычные CRUD ---------------- */

    @PostMapping
    public ResponseEntity<?> createVacation(@Valid @RequestBody Vacation vacation) {
        if (vacation.getEmployee() == null) {
            return ResponseEntity.badRequest().body("No employee specified");
        }
        Long empId = vacation.getEmployee().getId();
        if (!employeeRepository.existsById(empId)) {
            return ResponseEntity.badRequest().body("Employee not found: " + empId);
        }
        if (vacation.getStartDate().isAfter(vacation.getEndDate())) {
            return ResponseEntity.badRequest().body("startDate cannot be after endDate");
        }
        Vacation saved = vacationRepository.save(vacation);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Vacation> getAllVacations() {
        return vacationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vacation> getVacation(@PathVariable Long id) {
        return vacationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVacation(@PathVariable Long id,
                                            @Valid @RequestBody Vacation updated) {
        Optional<Vacation> opt = vacationRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Vacation v = opt.get();

        v.setEmployee(updated.getEmployee());
        v.setStartDate(updated.getStartDate());
        v.setEndDate(updated.getEndDate());
        v.setStatus(updated.getStatus());
        v.setComment(updated.getComment());

        // HR-поля не меняем здесь!

        vacationRepository.save(v);
        return ResponseEntity.ok(v);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacation(@PathVariable Long id) {
        if (!vacationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vacationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* ---------- HR-дополнительные ------------------------ */


    /** Список заявок, ожидающих решения HR */
    @GetMapping("/hr/pending")
    public List<Vacation> pending(HttpServletRequest request) {
        requireHr(request);
        return vacationRepository.findByStatus(VacationStatus.REQUESTED);
    }

    /** Одобрить заявку */
    @PostMapping("/hr/{id}/approve")
    public Vacation approve(@PathVariable Long id,
                            @RequestBody(required = false) Map<String,String> body,
                            HttpServletRequest request) {
        Employee hr = requireHr(request);

        Vacation v = vacationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        v.setStatus(VacationStatus.APPROVED);
        v.setApproverUsername(hr.getUsername());
        v.setApprovedAt(LocalDateTime.now());
        v.setHrComment(body != null ? body.get("comment") : null);

        return vacationRepository.save(v);
    }

    /** Отклонить заявку */
    @PostMapping("/hr/{id}/reject")
    public Vacation reject(@PathVariable Long id,
                           @RequestBody Map<String,String> body,
                           HttpServletRequest request) {
        Employee hr = requireHr(request);

        Vacation v = vacationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        v.setStatus(VacationStatus.REJECTED);
        v.setApproverUsername(hr.getUsername());
        v.setApprovedAt(LocalDateTime.now());
        v.setHrComment(body.getOrDefault("comment",""));

        return vacationRepository.save(v);
    }

    /* ---------- Календарный фид -------------------------- */

    @GetMapping("/calendar")
    public List<CalendarDTO> calendar() {
        return vacationRepository.findByStatus(VacationStatus.APPROVED)
                .stream()
                .map(v -> new CalendarDTO(
                        v.getEmployee().getUsername(),
                        v.getStartDate(),
                        v.getEndDate()))
                .toList();
    }
    public record CalendarDTO(String username, LocalDate start, LocalDate end) {}

    /* ---------- helpers --------------------------------- */

    private Employee requireHr(HttpServletRequest request) {
        String user = extractUsernameFromCookie(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Employee emp = employeeRepository.findByUsername(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (emp.getRole() != EmployeeRole.ADMIN && emp.getRole() != EmployeeRole.HR) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return emp;
    }

    private String currentUser() {
        Object val = RequestContextHolder.currentRequestAttributes()
                .getAttribute("username", RequestAttributes.SCOPE_SESSION);
        return val == null ? "anon" : val.toString();
    }

    private Optional<String> extractUsernameFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return Optional.empty();

        for (Cookie c : cookies) {
            if ("username".equals(c.getName())) {
                return Optional.ofNullable(c.getValue());
            }
        }
        return Optional.empty();
    }
}


