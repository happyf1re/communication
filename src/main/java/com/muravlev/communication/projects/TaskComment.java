package com.muravlev.communication.projects;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Привязка к задаче
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    // Автор комментария
    @NotBlank
    private String authorUsername;

    @NotBlank
    private String text;

    private LocalDateTime createdAt;
}

