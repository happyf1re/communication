package com.muravlev.communication.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    private String password; // демо-проект: хранение в plaintext

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String department;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
}
