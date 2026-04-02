package com.frontend.HospitalManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.frontend.HospitalManagement.dto.patient.PatientAddDto;
import com.frontend.HospitalManagement.dto.patient.PatientUpdateDto;
import com.frontend.HospitalManagement.service.PatientService;

import java.util.Map;

@Controller
public class PatientControllerFrontend {

    private final PatientService patientService;

    public PatientControllerFrontend(PatientService patientService) {
        this.patientService = patientService;
    }

    // ── List / Search patients
    @GetMapping("/patients")
    public String getPatients(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {

        Map<String, Object> data;

        if (name != null && !name.trim().isEmpty()) {
            data = patientService.searchPatientsByName(name, page, size);
        } else {
            data = patientService.getPatients(page, size);
        }

        model.addAttribute("patients",    data.get("patients"));
        model.addAttribute("currentPage", data.get("currentPage"));
        model.addAttribute("totalPages",  data.get("totalPages"));
        model.addAttribute("name",        name);

        return "patient/patients";
    }

    // ── Handle add-patient modal form submission 
    @PostMapping("/patients/add")
    public String handleAddPatient(
            @ModelAttribute PatientAddDto dto,
            RedirectAttributes redirectAttributes) {

        String result = patientService.addPatient(dto);

        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "Patient added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result);
        }

        return "redirect:/patients"; 
    }

    @GetMapping("/patients/edit/{ssn}")
    public String showEditPage(@PathVariable Integer ssn, Model model) {

        PatientUpdateDto dto = patientService.getPatientBySsn(ssn);
        model.addAttribute("patientUpdateDto", dto);
        model.addAttribute("ssn", ssn);

        return "patient/edit-patients";
    }

    //Handle update-patient modal form submission
    @PostMapping("/patients/update/{ssn}")
    public String handleUpdatePatient(
            @PathVariable Integer ssn,
            @ModelAttribute PatientUpdateDto dto,
            RedirectAttributes redirectAttributes) {

        boolean success = patientService.updatePatient(ssn, dto);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Patient updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update patient. Please try again.");
        }

        return "redirect:/patients";
    }
}