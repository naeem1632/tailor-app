package com.example.tailorapp.repository;

import com.example.tailorapp.model.PriceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSettingsRepository extends JpaRepository<PriceSettings, Long> {
}
