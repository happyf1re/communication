//package com.muravlev.communication.hr;
//
//import com.muravlev.communication.employee.Employee;
//import com.muravlev.communication.employee.EmployeeRole;
//import com.muravlev.communication.employee.EmployeeService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.server.ResponseStatusException;
//
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/vacations/hr")
//@RequiredArgsConstructor
//public class VacationHrController {
//
//    private final VacationRepository vacationRepo;
//    private final EmployeeService employeeService;
//
//    /** Получить все заявки в статусе REQUESTED */
//    @GetMapping("/pending")
//    public List<Vacation> pending() {
//        requireHR();
//        return vacationRepo.findByStatus(VacationStatus.REQUESTED);
//    }
//
//    /** Одобрить заявку */
//    @PostMapping("/{id}/approve")
//    public Vacation approve(@PathVariable Long id,
//                            @RequestBody(required = false) Map<String,String> body) {
//        requireHR();
//        Vacation v = vacationRepo.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//        v.setStatus(VacationStatus.APPROVED);
//        v.setApproverUsername(currentUser());
//        v.setApprovedAt(LocalDateTime.now());
//        v.setHrComment(body != null ? body.get("comment") : null);
//        return vacationRepo.save(v);
//    }
//
//    /** Отклонить заявку */
//    @PostMapping("/{id}/reject")
//    public Vacation reject(@PathVariable Long id,
//                           @RequestBody Map<String,String> body) {
//        requireHR();
//        Vacation v = vacationRepo.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//        v.setStatus(VacationStatus.REJECTED);
//        v.setApproverUsername(currentUser());
//        v.setApprovedAt(LocalDateTime.now());
//        v.setHrComment(body.getOrDefault("comment",""));
//        return vacationRepo.save(v);
//    }
//
//    // --- утилиты ------------------------------------------------------------
//    private void requireHR() {
//        Employee me = employeeService
//                .findByUsername(currentUser())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
//        if (me.getRole() != EmployeeRole.HR && me.getRole() != EmployeeRole.ADMIN) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
//        }
//    }
//    private String currentUser() {
//        return Optional.ofNullable(RequestContextHolder.currentRequestAttributes()
//                        .getAttribute("username", RequestAttributes.SCOPE_SESSION))
//                .map(Object::toString)
//                .orElse("anon");
//    }
//}

