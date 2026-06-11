package com.example.tailorapp.service;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAllByOrderByIdAsc();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> findActiveEmployees() {
        return employeeRepository.findByEmployeeStatusOrderByIdAsc("ACTIVE");
    }

    public List<Employee> findByStatus(String status) {
        return employeeRepository.findByEmployeeStatusOrderByIdAsc(status);
    }

    public List<Employee> searchByName(String name) {
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }

    // Find employees by skill/capability
    public List<Employee> findCutters() {
        return employeeRepository.findByCanCutTrueAndEmployeeStatus("ACTIVE");
    }

    public List<Employee> findPlainStitchers() {
        return employeeRepository.findByCanStitchPlainTrueAndEmployeeStatus("ACTIVE");
    }

    public List<Employee> findDesignStitchers() {
        return employeeRepository.findByCanStitchDesignTrueAndEmployeeStatus("ACTIVE");
    }

    public List<Employee> findFinishers() {
        return employeeRepository.findByCanDoFinishingTrueAndEmployeeStatus("ACTIVE");
    }

    public List<Employee> findEmbellishmentWorkers() {
        return employeeRepository.findByCanDoEmbellishmentTrueAndEmployeeStatus("ACTIVE");
    }

    // Get employees based on work type
    public List<Employee> getEmployeesForWorkType(String workType) {
        return switch (workType.toUpperCase()) {
            case "CUTTING" -> findCutters();
            case "STITCHING" -> findPlainStitchers();
            case "STITCHING_DESIGN" -> findDesignStitchers();
            case "FINISHING" -> findFinishers();
            case "EMBELLISHMENT" -> findEmbellishmentWorkers();
            default -> findActiveEmployees();
        };
    }
}