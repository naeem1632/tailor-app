package com.example.tailorapp.repository;

import com.example.tailorapp.model.ShirtMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShirtRepository extends JpaRepository<ShirtMeasurement, Long> {
    List<ShirtMeasurement> findByClientId(Long clientId);
}