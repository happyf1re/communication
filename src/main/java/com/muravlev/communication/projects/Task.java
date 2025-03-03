package com.muravlev.communication.projects;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь c проектом
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @NotBlank
    private String title;

    @NotBlank
    private String assigneeUsername; // Логин исполнителя

    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private String description;

    // Учёт затраченного времени (в минутах)
    private int timeSpentMinutes;
}

