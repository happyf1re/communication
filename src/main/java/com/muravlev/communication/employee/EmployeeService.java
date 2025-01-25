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

    public boolean authenticate(String username, String rawPassword) {
        Optional<Employee> employee = employeeRepository.findByUsername(username);
        return employee.isPresent() && employee.get().getPassword().equals(rawPassword);
    }

    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

}
