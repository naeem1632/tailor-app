package com.example.tailorapp.controller;

import com.example.tailorapp.model.PriceSettings;
import com.example.tailorapp.service.PriceSettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final PriceSettingsService priceSettingsService;

    public SettingsController(PriceSettingsService priceSettingsService) {
        this.priceSettingsService = priceSettingsService;
    }

    @GetMapping
    public String showSettings(Model model) {
        model.addAttribute("settings", priceSettingsService.getSettings());
        return "settings/index";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute PriceSettings settings, RedirectAttributes redirectAttributes) {
        priceSettingsService.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "Prices saved successfully.");
        return "redirect:/settings";
    }
}
