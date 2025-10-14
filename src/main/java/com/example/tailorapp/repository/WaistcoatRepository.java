package com.example.tailorapp.repository;

import com.example.tailorapp.model.WaistcoatMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaistcoatRepository extends JpaRepository<WaistcoatMeasurement, Long> {
    List<WaistcoatMeasurement> findByClientId(Long clientId);
}
