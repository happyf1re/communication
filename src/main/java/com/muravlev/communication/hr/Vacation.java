package com.muravlev.communication.hr;

import com.muravlev.communication.employee.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Vacation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Сотрудник, который подал заявку */
    @ManyToOne @JoinColumn(name = "employee_id")
    @NotNull
    private Employee employee;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    /** Текущее состояние заявки */
    @NotNull
    @Enumerated(EnumType.STRING)
    private VacationStatus status = VacationStatus.REQUESTED;

    /** Комментарий автора заявки */
    private String comment;

    /* ---------- новые поля для HR-процесса ---------- */

    /** Кто утвердил / отклонил */
    private String approverUsername;

    /** Когда утвердил / отклонил */
    private LocalDateTime approvedAt;

    /** Комментарий HR при approve/reject */
    private String hrComment;
}

