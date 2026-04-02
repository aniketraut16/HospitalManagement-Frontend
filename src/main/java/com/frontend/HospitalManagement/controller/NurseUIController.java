package com.frontend.HospitalManagement.controller;

import com.frontend.HospitalManagement.dto.Nurse.NurseDTO;
import com.frontend.HospitalManagement.dto.Nurse.NursePageResponse;
import com.frontend.HospitalManagement.dto.Nurse.NursePosition;
import com.frontend.HospitalManagement.service.NurseApiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class NurseUIController {

    @Autowired
    private NurseApiService nurseApiService;


    @GetMapping("/nurses")
    public String getNurses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) NursePosition positionFilter,
            Model model
    ) {

        NursePageResponse response =
                nurseApiService.getNurses(page, size, keyword, positionFilter);

        if (response != null && response.getNurses() != null) {
            model.addAttribute("nurses", response.getNurses());
            model.addAttribute("totalPages", response.getTotalPages());
        } else {
            model.addAttribute("nurses", java.util.Collections.emptyList());
            model.addAttribute("totalPages", 0);
        }

        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("positionFilter", positionFilter);
        model.addAttribute("positions", NursePosition.values());

        return "nurse/nurses";
    }


    @GetMapping("/nurses/add")
    public String showAddForm(Model model) {
        model.addAttribute("nurse", new NurseDTO());
        model.addAttribute("positions", NursePosition.values());
        return "nurse/add-nurse";
    }

    @PostMapping("/nurses/add")
    public String addNurse(@Valid @ModelAttribute NurseDTO nurse,
                           BindingResult result,
                           Model model) {

        if (result.hasErrors()) {
            model.addAttribute("positions", NursePosition.values());
            return "nurse/add-nurse";
        }

        nurseApiService.addNurse(nurse);
        return "redirect:/nurses";
    }


    @GetMapping("/nurses/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {

        NurseDTO nurse = nurseApiService.getNurseById(id);

        if (nurse == null) {
            return "redirect:/nurses";
        }

        model.addAttribute("nurse", nurse);
        model.addAttribute("positions", NursePosition.values());

        return "nurse/edit-nurse";
    }

    @PostMapping("/nurses/update")
    public String updateNurse(@Valid @ModelAttribute NurseDTO nurse,
                              BindingResult result,
                              Model model) {

        if (result.hasErrors()) {
            model.addAttribute("positions", NursePosition.values());
            return "nurse/edit-nurse";
        }

        nurseApiService.updateNurse(nurse.getEmployeeId(), nurse);

        return "redirect:/nurses";
    }

    @GetMapping("/nurses/view/{id}")
    public String viewNurse(@PathVariable Integer id, Model model) {
        NurseDTO nurse = nurseApiService.getNurseById(id);

        if (nurse == null) {
            return "redirect:/nurses";
        }

        Map<String, Object> onCall = nurseApiService.getOnCallByNurse(id);
        Map<String, Object> appointment = nurseApiService.getAppointmentByNurse(id);
        String status = nurseApiService.getNurseStatus(id);

        model.addAttribute("nurse", nurse);
        model.addAttribute("onCall", onCall);
        model.addAttribute("appointment", appointment);
        model.addAttribute("status", status);

        return "nurse/nurse-details";
    }
}
