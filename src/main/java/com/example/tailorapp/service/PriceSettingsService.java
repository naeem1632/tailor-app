package com.example.tailorapp.service;

import com.example.tailorapp.model.PriceSettings;
import com.example.tailorapp.repository.PriceSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PriceSettingsService {

    private final PriceSettingsRepository repo;

    public PriceSettingsService(PriceSettingsRepository repo) {
        this.repo = repo;
    }

    /** Returns the single settings row, creating a blank one if it doesn't exist yet. */
    public PriceSettings getSettings() {
        return repo.findById(1L).orElseGet(() -> repo.save(new PriceSettings()));
    }

    public PriceSettings save(PriceSettings settings) {
        settings.setId(1L); // always enforce single row
        return repo.save(settings);
    }
}
