package com.muravlev.communication.hr;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacationRepository extends JpaRepository<Vacation, Long> {

    // Можно сделать методы: найти все отпуска конкретного сотрудника
    List<Vacation> findByEmployeeId(Long employeeId);
}

