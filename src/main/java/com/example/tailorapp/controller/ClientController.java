package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.DressMeasurement;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.ShirtMeasurement;
import com.example.tailorapp.model.WaistcoatMeasurement;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.MeasurementService;
import com.example.tailorapp.service.PaymentsService;
import com.example.tailorapp.service.ShirtService;
import com.example.tailorapp.service.StorageProperties;

import com.example.tailorapp.service.WaistcoatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
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
    private final ShirtService shirtService;
    private final PaymentsService paymentsService;

    public ClientController(ClientService clientService,
                            MeasurementService measurementService,
                            StorageProperties storageProperties,
                            WaistcoatService waistcoatService,
                            ShirtService shirtService,
                            PaymentsService paymentsService) {
        this.clientService = clientService;
        this.measurementService = measurementService;
        this.storageProperties = storageProperties;
        this.waistcoatService = waistcoatService;
        this.shirtService = shirtService;
        this.paymentsService = paymentsService;
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
                       @RequestParam(required = false) Long editShirt,
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

        // Shirt measurements
        List<ShirtMeasurement> shirtMeasurements = shirtService.findByClient(id)
                .stream()
                .sorted(Comparator.comparing(ShirtMeasurement::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("shirtMeasurements", shirtMeasurements);

        // Form handling - Dress
        if (edit != null) {
            model.addAttribute("dressMeasurement", measurementService.findById(edit).orElse(new DressMeasurement()));
            model.addAttribute("formAction", "/clients/updateMeasurement/" + edit);
        } else {
            model.addAttribute("dressMeasurement", new DressMeasurement());
            model.addAttribute("formAction", "/clients/addMeasurement/" + client.getId());
        }

        // Form handling - Waistcoat
        if (editWaistcoat != null) {
            model.addAttribute("waistcoatMeasurement", waistcoatService.findById(editWaistcoat).orElse(new WaistcoatMeasurement()));
            model.addAttribute("waistcoatFormAction", "/clients/updateWaistcoatMeasurement/" + editWaistcoat);
        } else {
            model.addAttribute("waistcoatMeasurement", new WaistcoatMeasurement());
            model.addAttribute("waistcoatFormAction", "/clients/addWaistcoatMeasurement/" + client.getId());
        }

        // Form handling - Shirt
        if (editShirt != null) {
            model.addAttribute("shirtMeasurement", shirtService.findById(editShirt).orElse(new ShirtMeasurement()));
            model.addAttribute("shirtFormAction", "/clients/updateShirtMeasurement/" + editShirt);
        } else {
            model.addAttribute("shirtMeasurement", new ShirtMeasurement());
            model.addAttribute("shirtFormAction", "/clients/addShirtMeasurement/" + client.getId());
        }

        model.addAttribute("client", client);
        return "clients/view";
    }

    // View client details with payment history
    @GetMapping("/details/{id}")
    public String details(@PathVariable Long id, Model model) {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return "redirect:/clients";

        Client client = c.get();

        // Get all payments for this client sorted by date (newest first)
        List<Payments> payments = paymentsService.findByClient(id);
        payments.sort(Comparator.comparing(Payments::getDate).reversed());

        model.addAttribute("client", client);
        model.addAttribute("payments", payments);
        return "clients/details";
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
        // Copy all properties except id, client, and date to preserve the existing record metadata
        BeanUtils.copyProperties(dressMeasurement, m, "id", "client", "date");
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

        ra.addFlashAttribute("message", "Waistcoat measurement copied with today's date");
        return "redirect:/clients/view/" + original.getClient().getId();
    }

    // ===================== SHIRT ENDPOINTS =====================

    // Shirt Add
    @PostMapping("/addShirtMeasurement/{id}")
    public String addShirt(@PathVariable Long id,
                           @ModelAttribute ShirtMeasurement shirtMeasurement,
                           RedirectAttributes ra) {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) {
            ra.addFlashAttribute("error", "Client not found");
            return "redirect:/clients";
        }
        shirtMeasurement.setClient(c.get());
        shirtMeasurement.setDate(LocalDate.now());
        shirtService.save(shirtMeasurement);
        ra.addFlashAttribute("message", "Shirt Measurement added successfully");
        return "redirect:/clients/view/" + id;
    }

    // Shirt Update
    @PostMapping("/updateShirtMeasurement/{id}")
    public String updateShirt(@PathVariable Long id,
                              @ModelAttribute ShirtMeasurement shirtMeasurement,
                              RedirectAttributes ra) {
        Optional<ShirtMeasurement> existing = shirtService.findById(id);
        if (existing.isEmpty()) {
            ra.addFlashAttribute("error", "Measurement not found");
            return "redirect:/clients";
        }
        ShirtMeasurement db = existing.get();
        shirtMeasurement.setClient(db.getClient());
        shirtService.save(shirtMeasurement);
        ra.addFlashAttribute("message", "Shirt Measurement updated successfully");
        return "redirect:/clients/view/" + db.getClient().getId();
    }

    // Shirt Delete
    @GetMapping("/deleteShirtMeasurement/{id}")
    public String deleteShirt(@PathVariable Long id, RedirectAttributes ra) {
        Optional<ShirtMeasurement> m = shirtService.findById(id);
        if (m.isPresent()) {
            Long clientId = m.get().getClient().getId();
            shirtService.deleteById(id);
            ra.addFlashAttribute("message", "Shirt Measurement deleted successfully");
            return "redirect:/clients/view/" + clientId;
        }
        return "redirect:/clients";
    }

    // Shirt Copy
    @GetMapping("/copyShirtMeasurement/{id}")
    public String copyShirt(@PathVariable Long id, RedirectAttributes ra) {
        Optional<ShirtMeasurement> existing = shirtService.findById(id);
        if (existing.isEmpty()) {
            ra.addFlashAttribute("error", "Measurement not found");
            return "redirect:/clients";
        }
        ShirtMeasurement original = existing.get();
        ShirtMeasurement copy = new ShirtMeasurement();
        org.springframework.beans.BeanUtils.copyProperties(original, copy, "id", "date");
        copy.setDate(LocalDate.now());
        shirtService.save(copy);
        ra.addFlashAttribute("message", "Shirt measurement copied with today's date");
        return "redirect:/clients/view/" + original.getClient().getId();
    }

}
