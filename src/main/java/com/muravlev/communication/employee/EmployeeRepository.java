package com.muravlev.communication.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Для проверки существования
    boolean existsByUsername(String username);

    // Для поиска, если нужно
    Optional<Employee> findByUsername(String username);
}

