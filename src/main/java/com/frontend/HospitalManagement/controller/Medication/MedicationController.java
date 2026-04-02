package com.frontend.HospitalManagement.controller.Medication;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.frontend.HospitalManagement.dto.Medication.MedicationDTO;
import com.frontend.HospitalManagement.service.Medication.MedicationService;

@Controller
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService service;

    @GetMapping("/medications")
    public String getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            Model model) {

        int size = 10;

        Map<String, Object> result;

        if (keyword != null && !keyword.isEmpty()) {
            result = service.searchMedications(keyword, page, size);
            model.addAttribute("keyword", keyword);
        } else {
            result = service.getMedications(page, size);
        }

        model.addAttribute("medications", result.get("data"));

        Map<String, Object> pageInfo = (Map<String, Object>) result.get("page");

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageInfo.get("totalPages"));
        model.addAttribute("size", size);

        return "Medication/medications";
    }

    @GetMapping("/medications/add")
    public String showAddForm(Model model) {
        model.addAttribute("medication", new MedicationDTO()); // REQUIRED
        model.addAttribute("isEdit", false); // IMPORTANT 
        return "Medication/medication-form";
    }

    @GetMapping("/medications/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {

        MedicationDTO dto = service.getMedicationById(id); 

        model.addAttribute("medication", dto); // REQUIRED
        model.addAttribute("isEdit", true); // IMPORTANT

        return "Medication/medication-form";
    }

    @PostMapping("/medications/save")
    public String saveMedication(
            @ModelAttribute MedicationDTO dto,
            Model model
    ) {

        String error;

        if (dto.getCode() == null) {
            error = service.createMedication(dto);
        } else {
            error = service.updateMedication(dto);
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid input or duplicate code");
            model.addAttribute("medication", dto);
            model.addAttribute("isEdit", dto.getCode() != null);
            return "Medication/medication-form";
        }

        return "redirect:/medications";
    }
}
