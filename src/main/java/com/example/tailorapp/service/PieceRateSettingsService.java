package com.example.tailorapp.service;

import com.example.tailorapp.model.PieceRateSettings;
import com.example.tailorapp.repository.PieceRateSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PieceRateSettingsService {

    private final PieceRateSettingsRepository pieceRateSettingsRepository;

    public PieceRateSettingsService(PieceRateSettingsRepository pieceRateSettingsRepository) {
        this.pieceRateSettingsRepository = pieceRateSettingsRepository;
    }

    public List<PieceRateSettings> findAll() {
        return pieceRateSettingsRepository.findAllByOrderByEffectiveDateDesc();
    }

    public Optional<PieceRateSettings> findById(Long id) {
        return pieceRateSettingsRepository.findById(id);
    }

    public PieceRateSettings save(PieceRateSettings settings) {
        return pieceRateSettingsRepository.save(settings);
    }

    public void delete(Long id) {
        pieceRateSettingsRepository.deleteById(id);
    }

    /**
     * Get the current active piece rate settings (most recent)
     */
    public PieceRateSettings getCurrentSettings() {
        return pieceRateSettingsRepository.findFirstByOrderByEffectiveDateDesc()
                .orElseGet(this::createDefaultSettings);
    }

    /**
     * Get piece rate settings that were active on a specific date
     */
    public PieceRateSettings getSettingsForDate(LocalDate date) {
        List<PieceRateSettings> settings = pieceRateSettingsRepository
                .findByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(date);

        if (!settings.isEmpty()) {
            return settings.get(0); // Most recent one before or on the date
        }

        // Fallback to current settings
        return getCurrentSettings();
    }

    /**
     * Get rate for specific work type and item
     */
    public Long getRate(String workType, String itemType, Boolean isDesignWork, String embellishmentType) {
        PieceRateSettings settings = getCurrentSettings();
        return calculateRate(settings, workType, itemType, isDesignWork, embellishmentType);
    }

    /**
     * Get rate using specific settings (for historical accuracy)
     */
    public Long getRate(PieceRateSettings settings, String workType, String itemType,
                        Boolean isDesignWork, String embellishmentType) {
        return calculateRate(settings, workType, itemType, isDesignWork, embellishmentType);
    }

    private Long calculateRate(PieceRateSettings settings, String workType, String itemType,
                               Boolean isDesignWork, String embellishmentType) {
        if (settings == null) {
            return 0L;
        }

        switch (workType.toUpperCase()) {
            case "CUTTING":
                return getCuttingRate(settings, itemType);
            case "STITCHING":
                return getStitchingRate(settings, itemType, isDesignWork != null && isDesignWork);
            case "FINISHING":
                return settings.getFinishingRate();
            case "EMBELLISHMENT":
                return getEmbellishmentRate(settings, embellishmentType);
            default:
                return 0L;
        }
    }

    private Long getCuttingRate(PieceRateSettings settings, String itemType) {
        return switch (itemType.toUpperCase()) {
            case "DRESS" -> settings.getKameezCuttingRate() + settings.getShalwarCuttingRate(); // Dress = Kameez + Shalwar
            case "KAMEEZ" -> settings.getKameezCuttingRate();
            case "SHALWAR" -> settings.getShalwarCuttingRate();
            case "PAJAMA" -> settings.getPajamaCuttingRate();
            case "SHIRT" -> settings.getShirtCuttingRate();
            case "WAISTCOAT" -> settings.getWaistcoatCuttingRate();
            case "TROUSER" -> settings.getTrouserCuttingRate();
            default -> 0L;
        };
    }

    private Long getStitchingRate(PieceRateSettings settings, String itemType, boolean isDesignWork) {
        return switch (itemType.toUpperCase()) {
            case "DRESS" -> { // Dress = Kameez + Shalwar stitching
                Long kameezRate = isDesignWork ? settings.getKameezDesignStitchingRate() : settings.getKameezPlainStitchingRate();
                Long shalwarRate = isDesignWork ? settings.getShalwarDesignStitchingRate() : settings.getShalwarPlainStitchingRate();
                yield kameezRate + shalwarRate;
            }
            case "KAMEEZ" -> isDesignWork ?
                settings.getKameezDesignStitchingRate() : settings.getKameezPlainStitchingRate();
            case "SHALWAR" -> isDesignWork ?
                settings.getShalwarDesignStitchingRate() : settings.getShalwarPlainStitchingRate();
            case "PAJAMA" -> settings.getPajamaStitchingRate();
            case "SHIRT" -> settings.getShirtStitchingRate();
            case "WAISTCOAT" -> settings.getWaistcoatStitchingRate();
            case "TROUSER" -> settings.getTrouserStitchingRate();
            default -> 0L;
        };
    }

    private Long getEmbellishmentRate(PieceRateSettings settings, String embellishmentType) {
        if (embellishmentType == null) {
            return 0L;
        }
        return switch (embellishmentType.toUpperCase()) {
            case "MATEL" -> settings.getMatelWorkRate();
            case "TICH" -> settings.getTichWorkRate();
            case "KANTA" -> settings.getKantaWorkRate();
            case "JALI" -> settings.getJaliWorkRate();
            case "KRHAI" -> settings.getKrhaiWorkRate();
            default -> 0L;
        };
    }

    /**
     * Create default settings if none exist
     */
    private PieceRateSettings createDefaultSettings() {
        PieceRateSettings defaults = new PieceRateSettings();
        defaults.setEffectiveDate(LocalDate.now());

        // Set default cutting rates
        defaults.setKameezCuttingRate(50L);
        defaults.setShalwarCuttingRate(40L);
        defaults.setPajamaCuttingRate(40L);
        defaults.setShirtCuttingRate(50L);
        defaults.setWaistcoatCuttingRate(30L);

        // Set default plain stitching rates
        defaults.setKameezPlainStitchingRate(150L);
        defaults.setShalwarPlainStitchingRate(100L);
        defaults.setPajamaStitchingRate(100L);
        defaults.setShirtStitchingRate(200L);
        defaults.setWaistcoatStitchingRate(150L);

        // Set default design stitching rates
        defaults.setKameezDesignStitchingRate(300L);
        defaults.setShalwarDesignStitchingRate(200L);

        // Set default finishing rates
        defaults.setButtonWorkRate(20L);
        defaults.setEmbroideryRate(50L);
        defaults.setFinishingRate(30L);

        // Set default embellishment rates
        defaults.setMatelWorkRate(100L);
        defaults.setTichWorkRate(80L);
        defaults.setKantaWorkRate(120L);
        defaults.setJaliWorkRate(150L);
        defaults.setKrhaiWorkRate(100L);

        defaults.setNotes("Default rates - Please update as needed");

        return pieceRateSettingsRepository.save(defaults);
    }
}