package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.DressMeasurement;
import com.example.tailorapp.model.WaistcoatMeasurement;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.MeasurementService;
import com.example.tailorapp.service.StorageProperties;

import com.example.tailorapp.service.WaistcoatService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/clients")
@EnableConfigurationProperties(StorageProperties.class)
public class ClientController {

    private final ClientService clientService;
    private final MeasurementService measurementService;
    private final StorageProperties storageProperties;
    private final WaistcoatService waistcoatService;

    public ClientController(ClientService clientService,
                            MeasurementService measurementService,
                            StorageProperties storageProperties,
                            WaistcoatService waistcoatService) {
        this.clientService = clientService;
        this.measurementService = measurementService;
        this.storageProperties = storageProperties;
        this.waistcoatService = waistcoatService;
    }

    // List clients
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Client> clients = clientService.search(q);
        clients.sort(Comparator.comparing(Client::getId).reversed());
        model.addAttribute("clients", clients);
        model.addAttribute("q", q);
        return "clients/list";
    }

    @PostMapping("/save")
    public String saveClient(
            @ModelAttribute Client client,
            @RequestParam(value = "pictureFile", required = false) MultipartFile pictureFile,
            @RequestParam(value = "imageData", required = false) String imageData,
            RedirectAttributes ra) throws IOException {

        // ✅ Use path from application.properties
        String uploadDir = storageProperties.getClientPath();
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        // ✅ Load existing client (for update)
        Client existing = null;
        if (client.getId() != null) {
            existing = clientService.findById(client.getId()).orElse(null);
        }

        // ✅ Handle image upload
        if (pictureFile != null && !pictureFile.isEmpty()) {
            // File upload
            String filename = System.currentTimeMillis() + "_" + pictureFile.getOriginalFilename();
            File dest = new File(uploadPath, filename);
            pictureFile.transferTo(dest);
            client.setPictureFilename("/client-profiles/" + filename);
        }
        else if (imageData != null && !imageData.isEmpty()) {
            // Camera capture
            String base64Image = imageData.split(",")[1];
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            String filename = "camera_" + System.currentTimeMillis() + ".png";
            File outputFile = new File(uploadPath, filename);
            java.nio.file.Files.write(outputFile.toPath(), imageBytes);
            client.setPictureFilename("/client-profiles/" + filename);
        }
        else if (existing != null) {
            // ✅ Keep existing picture if no new one provided
            client.setPictureFilename(existing.getPictureFilename());
        }

        // ✅ Save client
        clientService.save(client);

        String msg = (existing != null) ? "Client updated successfully" : "Client added successfully";
        ra.addFlashAttribute("message", msg);

        return "redirect:/clients";
    }


    // View client + measurements
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id,
                       @RequestParam(required = false) Long edit,
                       @RequestParam(required = false) Long editWaistcoat,
                       Model model) {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return "redirect:/clients";

        Client client = c.get();

        // Dress measurements
        List<DressMeasurement> dressMeasurements = measurementService.findByClient(id)
                .stream()
                .sorted(Comparator.comparing(DressMeasurement::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("dressMeasurements", dressMeasurements);

        // Waistcoat measurements
        List<WaistcoatMeasurement> waistcoatMeasurements = waistcoatService.findByClient(id)
                .stream()
                .sorted(Comparator.comparing(WaistcoatMeasurement::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("waistcoatMeasurements", waistcoatMeasurements);

        // Form handling
        if (edit != null) {
            model.addAttribute("dressMeasurement", measurementService.findById(edit).orElse(new DressMeasurement()));
            model.addAttribute("formAction", "/clients/updateMeasurement/" + edit);
        } else {
            model.addAttribute("dressMeasurement", new DressMeasurement());
            model.addAttribute("formAction", "/clients/addMeasurement/" + client.getId());
        }

        if (editWaistcoat != null) {
            model.addAttribute("waistcoatMeasurement", waistcoatService.findById(editWaistcoat).orElse(new WaistcoatMeasurement()));
            model.addAttribute("waistcoatFormAction", "/clients/updateWaistcoatMeasurement/" + editWaistcoat);
        } else {
            model.addAttribute("waistcoatMeasurement", new WaistcoatMeasurement());
            model.addAttribute("waistcoatFormAction", "/clients/addWaistcoatMeasurement/" + client.getId());
        }

        model.addAttribute("client", client);
        return "clients/view";
    }
    // Add dressMeasurement
    @PostMapping("/addMeasurement/{id}")
    public String addMeasurement(@PathVariable Long id,
                                 @ModelAttribute("dressMeasurement") DressMeasurement dressMeasurement,
                                 RedirectAttributes ra) {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return "redirect:/clients";

        dressMeasurement.setClient(c.get());
        dressMeasurement.setId(null);
        dressMeasurement.setDate(LocalDate.now());
        measurementService.save(dressMeasurement);

        ra.addFlashAttribute("message", "DressMeasurement added successfully");
        return "redirect:/clients/view/" + id;
    }

    // Update dressMeasurement
    @PostMapping("/updateMeasurement/{measurementId}")
    public String updateMeasurement(@PathVariable Long measurementId,
                                    @ModelAttribute("dressMeasurement") DressMeasurement dressMeasurement,
                                    RedirectAttributes ra) {
        Optional<DressMeasurement> existing = measurementService.findById(measurementId);
        if (existing.isEmpty()) {
            ra.addFlashAttribute("error", "DressMeasurement not found");
            return "redirect:/clients";
        }

        DressMeasurement m = existing.get();
        m.setKameezLength(dressMeasurement.getKameezLength());
        m.setArm(dressMeasurement.getArm());
        m.setUpperArm(dressMeasurement.getUpperArm());
        m.setCenterArm(dressMeasurement.getCenterArm());
        m.setLowerArm(dressMeasurement.getLowerArm());
        m.setTerra(dressMeasurement.getTerra());
        m.setTerraDown(dressMeasurement.getTerraDown());
        m.setShoulderArm(dressMeasurement.getShoulderArm());
        m.setChest(dressMeasurement.getChest());
        m.setChestFitting(dressMeasurement.getChestFitting());
        m.setWaist(dressMeasurement.getWaist());
        m.setHip(dressMeasurement.getHip());
        m.setRound(dressMeasurement.getRound());
        m.setCollarSize(dressMeasurement.getCollarSize());
        m.setCollarType(dressMeasurement.getCollarType());
        m.setBainSize(dressMeasurement.getBainSize());
        m.setBainType(dressMeasurement.getBainType());
        m.setDamanType(dressMeasurement.getDamanType());
        m.setDamanStitching(dressMeasurement.getDamanStitching());
        m.setSidePocket(dressMeasurement.getSidePocket());
        m.setFrontPocket(dressMeasurement.getFrontPocket());
        m.setFrontPocketType(dressMeasurement.getFrontPocketType());
        m.setCuffDesign(dressMeasurement.getCuffDesign());
        m.setCuffLength(dressMeasurement.getCuffLength());
        m.setCuffWidth(dressMeasurement.getCuffWidth());
        m.setCuffType(dressMeasurement.getCuffType());
        m.setWristType(dressMeasurement.getWristType());
        m.setShalwarLength(dressMeasurement.getShalwarLength());
        m.setShalwarFitting(dressMeasurement.getShalwarFitting());
        m.setAsan(dressMeasurement.getAsan());
        m.setPayncha(dressMeasurement.getPayncha());
        m.setJali(dressMeasurement.getJali());
        m.setKanta(dressMeasurement.getKanta());
        m.setStitchType(dressMeasurement.getStitchType());
        m.setDesignStitch(dressMeasurement.getDesignStitch());
        m.setButtonType(dressMeasurement.getButtonType());
        m.setFrontPattiKaj(dressMeasurement.getFrontPattiKaj());
        m.setFrontPattiType(dressMeasurement.getFrontPattiType());
        m.setNotes(dressMeasurement.getNotes());
        m.setShalwarPocket(dressMeasurement.getShalwarPocket());

        m.setDressQty(dressMeasurement.getDressQty());
        m.setWithCollar(dressMeasurement.getWithCollar());
        m.setWithBain(dressMeasurement.getWithBain());
        m.setWithDesign(dressMeasurement.getWithDesign());
        measurementService.save(m);

        ra.addFlashAttribute("message", "DressMeasurement updated successfully");
        return "redirect:/clients/view/" + m.getClient().getId();
    }

    // Delete
    @GetMapping("/deleteMeasurement/{measurementId}")
    public String deleteMeasurement(@PathVariable Long measurementId, RedirectAttributes ra) {
        Optional<DressMeasurement> opt = measurementService.findById(measurementId);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "DressMeasurement not found");
            return "redirect:/clients";
        }
        DressMeasurement m = opt.get();
        Long clientId = m.getClient().getId();

        measurementService.deleteById(measurementId);
        ra.addFlashAttribute("message", "DressMeasurement deleted successfully");

        return "redirect:/clients/view/" + clientId;
    }

    @GetMapping("/copyMeasurement/{id}")
    public String copyMeasurement(@PathVariable Long id, RedirectAttributes ra) {
        Optional<DressMeasurement> existing = measurementService.findById(id);
        if (existing.isEmpty()) {
            ra.addFlashAttribute("error", "Measurement not found");
            return "redirect:/clients";
        }

        DressMeasurement original = existing.get();
        DressMeasurement copy = new DressMeasurement();
        org.springframework.beans.BeanUtils.copyProperties(original, copy, "id", "date");
        copy.setDate(LocalDate.now());
        measurementService.save(copy);

        ra.addFlashAttribute("message", "Measurement copied with today’s date");
        return "redirect:/clients/view/" + original.getClient().getId();
    }


    // Waistcoat Add
    @PostMapping("/addWaistcoatMeasurement/{id}")
    public String addWaistcoat(@PathVariable Long id,
                               @ModelAttribute WaistcoatMeasurement waistcoatMeasurement,
                               RedirectAttributes ra) {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) {
            ra.addFlashAttribute("error", "Client not found");
            return "redirect:/clients";
        }

        waistcoatMeasurement.setClient(c.get());
        waistcoatMeasurement.setDate(LocalDate.now());
        waistcoatService.save(waistcoatMeasurement);

        ra.addFlashAttribute("message", "Waistcoat Measurement added successfully");
        return "redirect:/clients/view/" + id;
    }

    // Waistcoat Update
    @PostMapping("/updateWaistcoatMeasurement/{id}")
    public String updateWaistcoat(@PathVariable Long id,
                                  @ModelAttribute WaistcoatMeasurement waistcoatMeasurement,
                                  RedirectAttributes ra) {

        Optional<WaistcoatMeasurement> existing = waistcoatService.findById(id);
        if (existing.isEmpty()) {
            ra.addFlashAttribute("error", "Measurement not found");
            return "redirect:/clients";
        }

        // Keep the existing client reference
        WaistcoatMeasurement dbWaistcoat = existing.get();
        waistcoatMeasurement.setClient(dbWaistcoat.getClient());

        waistcoatService.save(waistcoatMeasurement);
        ra.addFlashAttribute("message", "Waistcoat Measurement updated successfully");

        return "redirect:/clients/view/" + dbWaistcoat.getClient().getId();
    }

    // Waistcoat Delete
    @GetMapping("/deleteWaistcoatMeasurement/{id}")
    public String deleteWaistcoat(@PathVariable Long id, RedirectAttributes ra) {
        Optional<WaistcoatMeasurement> m = waistcoatService.findById(id);
        if (m.isPresent()) {
            Long clientId = m.get().getClient().getId();
            waistcoatService.deleteById(id);
            ra.addFlashAttribute("message", "Waistcoat Measurement deleted successfully");
            return "redirect:/clients/view/" + clientId;
        }
        return "redirect:/clients";
    }

    @GetMapping("/copyWaistcoatMeasurement/{id}")
    public String copyWaistcoat(@PathVariable Long id, RedirectAttributes ra) {
        Optional<WaistcoatMeasurement> existing = waistcoatService.findById(id);
        if (existing.isEmpty()) {
            ra.addFlashAttribute("error", "Measurement not found");
            return "redirect:/clients";
        }

        WaistcoatMeasurement original = existing.get();
        WaistcoatMeasurement copy = new WaistcoatMeasurement();
        org.springframework.beans.BeanUtils.copyProperties(original, copy, "id", "date");
        copy.setDate(LocalDate.now());
        waistcoatService.save(copy);

        ra.addFlashAttribute("message", "Waistcoat measurement copied with today’s date");
        return "redirect:/clients/view/" + original.getClient().getId();
    }

}
