package com.example.tailorapp.controller;

import com.example.tailorapp.model.PieceRateSettings;
import com.example.tailorapp.service.PieceRateSettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/piece-rate-settings")
public class PieceRateSettingsController {

    private final PieceRateSettingsService pieceRateSettingsService;

    public PieceRateSettingsController(PieceRateSettingsService pieceRateSettingsService) {
        this.pieceRateSettingsService = pieceRateSettingsService;
    }

    /**
     * List all piece rate settings (history)
     */
    @GetMapping
    public String listSettings(Model model) {
        List<PieceRateSettings> settingsList = pieceRateSettingsService.findAll();
        PieceRateSettings currentSettings = pieceRateSettingsService.getCurrentSettings();

        model.addAttribute("settingsList", settingsList);
        model.addAttribute("currentSettings", currentSettings);
        return "piece-rate-settings/index";
    }

    /**
     * Show form to add new rate settings
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        PieceRateSettings currentSettings = pieceRateSettingsService.getCurrentSettings();

        // Create new settings pre-filled with current rates for convenience
        PieceRateSettings newSettings = new PieceRateSettings();
        if (currentSettings != null) {
            // Copy current rates
            newSettings.setKameezCuttingRate(currentSettings.getKameezCuttingRate());
            newSettings.setShalwarCuttingRate(currentSettings.getShalwarCuttingRate());
            newSettings.setPajamaCuttingRate(currentSettings.getPajamaCuttingRate());
            newSettings.setShirtCuttingRate(currentSettings.getShirtCuttingRate());
            newSettings.setWaistcoatCuttingRate(currentSettings.getWaistcoatCuttingRate());

            newSettings.setKameezPlainStitchingRate(currentSettings.getKameezPlainStitchingRate());
            newSettings.setShalwarPlainStitchingRate(currentSettings.getShalwarPlainStitchingRate());
            newSettings.setPajamaStitchingRate(currentSettings.getPajamaStitchingRate());
            newSettings.setShirtStitchingRate(currentSettings.getShirtStitchingRate());
            newSettings.setWaistcoatStitchingRate(currentSettings.getWaistcoatStitchingRate());

            newSettings.setKameezDesignStitchingRate(currentSettings.getKameezDesignStitchingRate());
            newSettings.setShalwarDesignStitchingRate(currentSettings.getShalwarDesignStitchingRate());

            newSettings.setButtonWorkRate(currentSettings.getButtonWorkRate());
            newSettings.setEmbroideryRate(currentSettings.getEmbroideryRate());
            newSettings.setFinishingRate(currentSettings.getFinishingRate());

            newSettings.setMatelWorkRate(currentSettings.getMatelWorkRate());
            newSettings.setTichWorkRate(currentSettings.getTichWorkRate());
            newSettings.setKantaWorkRate(currentSettings.getKantaWorkRate());
            newSettings.setJaliWorkRate(currentSettings.getJaliWorkRate());
            newSettings.setKrhaiWorkRate(currentSettings.getKrhaiWorkRate());
        }

        model.addAttribute("settings", newSettings);
        model.addAttribute("currentSettings", currentSettings);
        return "piece-rate-settings/add";
    }

    /**
     * Save new rate settings
     */
    @PostMapping("/add")
    public String addSettings(@Valid @ModelAttribute("settings") PieceRateSettings settings,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "piece-rate-settings/add";
        }

        pieceRateSettingsService.save(settings);
        redirectAttributes.addFlashAttribute("success",
            "New piece rate settings saved successfully! Effective from: " + settings.getEffectiveDate());
        return "redirect:/piece-rate-settings";
    }

    /**
     * Show form to edit rate settings
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PieceRateSettings settings = pieceRateSettingsService.findById(id)
                .orElse(null);

        if (settings == null) {
            redirectAttributes.addFlashAttribute("error", "Settings not found!");
            return "redirect:/piece-rate-settings";
        }

        model.addAttribute("settings", settings);
        return "piece-rate-settings/edit";
    }

    /**
     * Update rate settings
     */
    @PostMapping("/edit/{id}")
    public String updateSettings(@PathVariable Long id,
                                @Valid @ModelAttribute("settings") PieceRateSettings settings,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "piece-rate-settings/edit";
        }

        settings.setId(id);
        pieceRateSettingsService.save(settings);
        redirectAttributes.addFlashAttribute("success", "Settings updated successfully!");
        return "redirect:/piece-rate-settings";
    }

    /**
     * View rate settings details
     */
    @GetMapping("/view/{id}")
    public String viewSettings(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        PieceRateSettings settings = pieceRateSettingsService.findById(id)
                .orElse(null);

        if (settings == null) {
            redirectAttributes.addFlashAttribute("error", "Settings not found!");
            return "redirect:/piece-rate-settings";
        }

        model.addAttribute("settings", settings);
        return "piece-rate-settings/view";
    }

    /**
     * Delete rate settings
     */
    @PostMapping("/delete/{id}")
    public String deleteSettings(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            PieceRateSettings settings = pieceRateSettingsService.findById(id).orElse(null);
            PieceRateSettings currentSettings = pieceRateSettingsService.getCurrentSettings();

            // Prevent deleting the only/current settings
            if (settings != null && currentSettings != null && settings.getId().equals(currentSettings.getId())) {
                List<PieceRateSettings> allSettings = pieceRateSettingsService.findAll();
                if (allSettings.size() == 1) {
                    redirectAttributes.addFlashAttribute("error",
                            "Cannot delete the only piece rate settings. Please add new settings first.");
                    return "redirect:/piece-rate-settings";
                }
            }

            pieceRateSettingsService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Settings deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting settings: " + e.getMessage());
        }
        return "redirect:/piece-rate-settings";
    }

    /**
     * Get current active rates (AJAX endpoint)
     */
    @GetMapping("/current")
    @ResponseBody
    public PieceRateSettings getCurrentSettings() {
        return pieceRateSettingsService.getCurrentSettings();
    }

    /**
     * Calculate rate for specific work (AJAX endpoint)
     */
    @GetMapping("/calculate-rate")
    @ResponseBody
    public Long calculateRate(@RequestParam String workType,
                             @RequestParam String itemType,
                             @RequestParam(required = false) Boolean isDesignWork,
                             @RequestParam(required = false) String embellishmentType) {
        return pieceRateSettingsService.getRate(workType, itemType, isDesignWork, embellishmentType);
    }
}