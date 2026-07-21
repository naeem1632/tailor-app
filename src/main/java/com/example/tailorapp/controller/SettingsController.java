package com.example.tailorapp.controller;

import com.example.tailorapp.model.PriceSettings;
import com.example.tailorapp.service.PriceSettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final PriceSettingsService priceSettingsService;

    public SettingsController(PriceSettingsService priceSettingsService) {
        this.priceSettingsService = priceSettingsService;
    }

    /**
     * List all price settings with history
     */
    @GetMapping
    public String listSettings(Model model) {
        List<PriceSettings> settingsList = priceSettingsService.getAllSettings();
        Optional<PriceSettings> currentSettings = priceSettingsService.getCurrentSettings();

        model.addAttribute("settingsList", settingsList);
        model.addAttribute("currentSettings", currentSettings.orElse(null));
        return "settings/index";
    }

    /**
     * Show form to add new price settings
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Optional<PriceSettings> currentSettingsOpt = priceSettingsService.getCurrentSettings();

        // Create new settings pre-filled with current prices for convenience
        PriceSettings newSettings = new PriceSettings();
        if (currentSettingsOpt.isPresent()) {
            PriceSettings currentSettings = currentSettingsOpt.get();
            newSettings.setDressRate(currentSettings.getDressRate());
            newSettings.setWaistcoatRate(currentSettings.getWaistcoatRate());
            newSettings.setShirtRate(currentSettings.getShirtRate());
            newSettings.setTrouserRate(currentSettings.getTrouserRate());
            newSettings.setMatelRate(currentSettings.getMatelRate());
            newSettings.setTichRate(currentSettings.getTichRate());
            newSettings.setKantaRate(currentSettings.getKantaRate());
            newSettings.setJaliRate(currentSettings.getJaliRate());
            newSettings.setKrhaiRate(currentSettings.getKrhaiRate());
        }

        model.addAttribute("settings", newSettings);
        model.addAttribute("currentSettings", currentSettingsOpt.orElse(null));
        return "settings/add";
    }

    /**
     * Save new price settings
     */
    @PostMapping("/save")
    public String saveSettings(@ModelAttribute PriceSettings settings, RedirectAttributes redirectAttributes) {
        priceSettingsService.save(settings);
        redirectAttributes.addFlashAttribute("success", "Price settings saved successfully.");
        return "redirect:/settings";
    }

    /**
     * Show view page for specific settings
     */
    @GetMapping("/view/{id}")
    public String viewSettings(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<PriceSettings> settingsOpt = priceSettingsService.findById(id);
        if (settingsOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Settings not found.");
            return "redirect:/settings";
        }
        model.addAttribute("settings", settingsOpt.get());
        return "settings/view";
    }

    /**
     * Show edit form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<PriceSettings> settingsOpt = priceSettingsService.findById(id);
        if (settingsOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Settings not found.");
            return "redirect:/settings";
        }
        model.addAttribute("settings", settingsOpt.get());
        return "settings/edit";
    }

    /**
     * Update existing settings
     */
    @PostMapping("/update/{id}")
    public String updateSettings(@PathVariable Long id, @ModelAttribute PriceSettings settings, RedirectAttributes redirectAttributes) {
        settings.setId(id);
        priceSettingsService.save(settings);
        redirectAttributes.addFlashAttribute("success", "Price settings updated successfully.");
        return "redirect:/settings";
    }
}
