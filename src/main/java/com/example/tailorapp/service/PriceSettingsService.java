package com.example.tailorapp.service;

import com.example.tailorapp.model.PriceSettings;
import com.example.tailorapp.repository.PriceSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PriceSettingsService {

    private final PriceSettingsRepository repo;

    public PriceSettingsService(PriceSettingsRepository repo) {
        this.repo = repo;
    }

    /** Returns current active price settings (most recent by effective date) */
    public PriceSettings getSettings() {
        return repo.findAll().stream()
                .max(Comparator.comparing(PriceSettings::getEffectiveDate))
                .orElseGet(() -> {
                    // Create default settings if none exist
                    PriceSettings defaultSettings = new PriceSettings();
                    defaultSettings.setEffectiveDate(LocalDate.now());
                    defaultSettings.setNotes("Initial default prices");
                    return repo.save(defaultSettings);
                });
    }

    /** Get all price settings ordered by effective date (newest first) */
    public List<PriceSettings> getAllSettings() {
        List<PriceSettings> list = repo.findAll();
        list.sort(Comparator.comparing(PriceSettings::getEffectiveDate).reversed());
        return list;
    }

    /** Get current active settings */
    public Optional<PriceSettings> getCurrentSettings() {
        return repo.findAll().stream()
                .max(Comparator.comparing(PriceSettings::getEffectiveDate));
    }

    /** Find by ID */
    public Optional<PriceSettings> findById(Long id) {
        return repo.findById(id);
    }

    /** Save new or update existing settings */
    public PriceSettings save(PriceSettings settings) {
        if (settings.getEffectiveDate() == null) {
            settings.setEffectiveDate(LocalDate.now());
        }
        return repo.save(settings);
    }

    /** Delete settings by ID */
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
