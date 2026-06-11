package com.example.tailorapp.repository;

import com.example.tailorapp.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find all employees ordered by ID
    List<Employee> findAllByOrderByIdAsc();

    // Find employees by status
    List<Employee> findByEmployeeStatus(String status);

    List<Employee> findByEmployeeStatusOrderByIdAsc(String status);

    // Find employees by name (case-insensitive search)
    List<Employee> findByNameContainingIgnoreCase(String name);

    // Find employees who can do cutting
    List<Employee> findByCanCutTrueAndEmployeeStatus(String status);

    // Find employees who can do stitching (plain or design)
    List<Employee> findByCanStitchPlainTrueAndEmployeeStatus(String status);

    // Find employees who can do design stitching
    List<Employee> findByCanStitchDesignTrueAndEmployeeStatus(String status);

    // Find employees who can do finishing
    List<Employee> findByCanDoFinishingTrueAndEmployeeStatus(String status);

    // Find employees who can do embellishment
    List<Employee> findByCanDoEmbellishmentTrueAndEmployeeStatus(String status);
}