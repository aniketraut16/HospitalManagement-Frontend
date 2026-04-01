package com.frontend.HospitalManagement.controller.Medication;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.frontend.HospitalManagement.dto.Medication.PrescriptionDTO;
import com.frontend.HospitalManagement.service.Medication.MedicationService;
import com.frontend.HospitalManagement.service.Medication.PrescriptionService;

@Controller
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final MedicationService medicationService;

    @GetMapping("/prescriptions")
    public String viewPrescriptions(
            @RequestParam int medicationId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int size = 10;

        Map<String, Object> response =
                prescriptionService.getPrescriptionsByMedication(medicationId, page, size);

        //  Data
        model.addAttribute("prescriptions", response.get("data"));

        //  Page info (SAFE handling)
        Map<String, Object> pageData =
                (Map<String, Object>) response.get("page");

        if (pageData != null) {
            model.addAttribute("currentPage", pageData.get("number"));
            model.addAttribute("totalPages", pageData.get("totalPages"));
        } else {
            // fallback
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
        }

        model.addAttribute("size", size);
        model.addAttribute("medicationId", medicationId);

        // ADD THIS LINE (IMPORTANT)
        String medicationName = medicationService.getMedicationNameById(medicationId);
        model.addAttribute("medicationName", medicationName);

        return "Medication/prescriptions";
    }
}