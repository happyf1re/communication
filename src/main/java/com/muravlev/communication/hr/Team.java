package com.muravlev.communication.hr;

import com.muravlev.communication.employee.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * "Команда" (группа сотрудников).
 * У каждой есть уникальный ID, название, опционально руководитель,
 * а также список участников (ManyToMany).
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name; // Название команды / отдела

    // Руководитель, опционально
    private String managerUsername;

    // Много сотрудников - много команд:
    // либо ManyToMany (через join-table), либо OneToMany.
    // Здесь ManyToMany для примера.
    @ManyToMany
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> members = new HashSet<>();
}

