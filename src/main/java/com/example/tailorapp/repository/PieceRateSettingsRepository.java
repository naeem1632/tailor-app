package com.example.tailorapp.repository;

import com.example.tailorapp.model.PieceRateSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PieceRateSettingsRepository extends JpaRepository<PieceRateSettings, Long> {

    // Find rates by effective date (most recent first)
    List<PieceRateSettings> findAllByOrderByEffectiveDateDesc();

    // Find rates that were effective on or before a specific date
    List<PieceRateSettings> findByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate date);

    // Find the most recent rate settings
    Optional<PieceRateSettings> findFirstByOrderByEffectiveDateDesc();
}