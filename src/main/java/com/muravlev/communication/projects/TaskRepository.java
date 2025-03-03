package com.muravlev.communication.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssigneeUsername(String username);
}