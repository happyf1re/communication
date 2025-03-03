package com.muravlev.communication.projects;

import jakarta.persistence.*;
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

    // Связь со "старым" проектом
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    private String title;
    private String assigneeUsername; // Тоже связка по строке
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private String description;
}
