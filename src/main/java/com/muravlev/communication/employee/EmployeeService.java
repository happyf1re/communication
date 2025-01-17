package com.muravlev.communication.employee;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee register(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
