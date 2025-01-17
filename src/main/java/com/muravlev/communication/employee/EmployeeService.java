package com.muravlev.communication.employee;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

//    public Employee register(Employee employee) {
//        // Хэшируем пароль перед сохранением
//        employee.setPassword(hashPassword(employee.getPassword()));
//        return employeeRepository.save(employee);
//    }

    public Employee register(Employee employee) {
        // Сохраняем пароль без хэширования
        return employeeRepository.save(employee);
    }

//    public boolean authenticate(String username, String rawPassword) {
//        Optional<Employee> employee = employeeRepository.findByUsername(username);
//        return employee.isPresent() && employee.get().getPassword().equals(hashPassword(rawPassword));
//    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<Employee> employee = employeeRepository.findByUsername(username);
        // Сравниваем пароль без хэширования
        return employee.isPresent() && employee.get().getPassword().equals(rawPassword);
    }

//    public String hashPassword(String rawPassword) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] encodedHash = digest.digest(rawPassword.getBytes());
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : encodedHash) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//            return hexString.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("Error hashing password", e);
//        }
//    }
}
