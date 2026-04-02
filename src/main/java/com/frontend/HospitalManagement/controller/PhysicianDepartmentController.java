package com.frontend.HospitalManagement.controller;

import com.frontend.HospitalManagement.dto.Affiliatedto.AffiliationDTO;
import com.frontend.HospitalManagement.dto.Physiciandto.PhysicianDTO;
import com.frontend.HospitalManagement.service.HospitalApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class PhysicianDepartmentController {

    private final HospitalApiService apiService;

    public PhysicianDepartmentController(HospitalApiService apiService) {
        this.apiService = apiService;
    }


    // ─── PHYSICIANS ─────────────────────────────────────────────────────

    @GetMapping("/physicians")
    public String listPhysicians(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) String searchType) {

        Map<String, Object> result;

        try {
            if (search != null && !search.trim().isEmpty()) {
                if ("position".equalsIgnoreCase(searchType)) {
                    result = apiService.searchPhysiciansByPosition(search.trim(), page, size);
                } else if ("ssn".equalsIgnoreCase(searchType)) {
                    try {
                        Long ssn = Long.parseLong(search.trim());
                        result = apiService.searchPhysicianBySsn(ssn);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        result = new java.util.LinkedHashMap<>();
                        result.put("physicians", java.util.Collections.emptyList());
                        result.put("currentPage", 0);
                        result.put("totalPages", 1);
                        result.put("totalElements", 0L);
                        result.put("error", "SSN must be a valid number.");
                    }
                } else {
                    result = apiService.searchPhysiciansByName(search.trim(), page, size);
                }
            } else {
                result = apiService.getAllPhysicians(page, size);
            }

            model.addAttribute("physicians", result.get("physicians"));
            model.addAttribute("currentPage", result.get("currentPage"));
            model.addAttribute("totalPages", result.get("totalPages"));
            model.addAttribute("totalElements", result.get("totalElements"));

            if (result.containsKey("error")) {
                model.addAttribute("error", result.get("error"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load physicians: " + e.getMessage());
        }

        model.addAttribute("search", search);
        model.addAttribute("searchType", searchType);
        model.addAttribute("activePage", "physicians");

        // FIX: Added the folder path here
        return "DepartmentAndPhysician/physicians";
    }

    @GetMapping("/physicians/{id}")
    public String physicianDetail(@PathVariable Integer id, Model model) {
        try {
            PhysicianDTO physician = apiService.getPhysicianById(id);
            List<AffiliationDTO> affiliations = apiService.getAffiliationsByPhysician(id);

            model.addAttribute("physician", physician);
            model.addAttribute("affiliations", affiliations);
            model.addAttribute("activePage", "physicians");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Physician not found: " + e.getMessage());
            return "redirect:/physicians";
        }

        // FIX: Added the folder path here
        return "DepartmentAndPhysician/physician-detail";
    }

    @GetMapping("/physicians/new")
    public String createPhysicianForm(Model model) {
        model.addAttribute("physician", new PhysicianDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("activePage", "physicians");
        
        // FIX: Added the folder path here
        return "DepartmentAndPhysician/physician-form";
    }

    @PostMapping("/physicians/new")
    public String createPhysician(@Valid @ModelAttribute("physician") PhysicianDTO physician,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("activePage", "physicians");
            return "DepartmentAndPhysician/physician-form";
        }

        try {
            Map<String, Object> ssnCheck = apiService.searchPhysicianBySsn(physician.getSsn());
            List<PhysicianDTO> existing = (List<PhysicianDTO>) ssnCheck.get("physicians");
            if (existing != null && !existing.isEmpty()) {
                model.addAttribute("error", "Failed to create physician: SSN already exists.");
                model.addAttribute("isEdit", false);
                model.addAttribute("activePage", "physicians");
                return "DepartmentAndPhysician/physician-form";
            }

            apiService.createPhysician(physician);
            redirectAttributes.addFlashAttribute("success", "Physician created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to create physician: " + e.getMessage());
        }
        return "redirect:/physicians";
    }

    @GetMapping("/physicians/{id}/edit")
    public String editPhysicianForm(@PathVariable Integer id, Model model) {
        try {
            PhysicianDTO physician = apiService.getPhysicianById(id);
            model.addAttribute("physician", physician);
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "physicians");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/physicians";
        }
        
        // FIX: Added the folder path here
        return "DepartmentAndPhysician/physician-form";
    }

    @PostMapping("/physicians/{id}/edit")
    public String updatePhysician(@PathVariable Integer id,
                                   @Valid @ModelAttribute("physician") PhysicianDTO physician,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "physicians");
            return "DepartmentAndPhysician/physician-form";
        }

        try {
            Map<String, Object> ssnCheck = apiService.searchPhysicianBySsn(physician.getSsn());
            List<PhysicianDTO> existing = (List<PhysicianDTO>) ssnCheck.get("physicians");
            if (existing != null && !existing.isEmpty()) {
                PhysicianDTO existingPhysician = existing.get(0);
                if (!existingPhysician.getEmployeeId().equals(id)) {
                    model.addAttribute("error", "Failed to update physician: SSN already exists.");
                    model.addAttribute("isEdit", true);
                    model.addAttribute("activePage", "physicians");
                    return "DepartmentAndPhysician/physician-form";
                }
            }

            apiService.updatePhysician(id, physician);
            redirectAttributes.addFlashAttribute("success", "Physician updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update physician: " + e.getMessage());
        }
        return "redirect:/physicians/" + id;
    }

    @PostMapping("/physicians/{id}/delete")
    public String deletePhysician(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            apiService.deletePhysician(id);
            redirectAttributes.addFlashAttribute("success", "Physician deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete physician: " + e.getMessage());
        }
        return "redirect:/physicians";
    }

    // ─── DEPARTMENTS ────────────────────────────────────────────────────

    @GetMapping("/departments")
    public String listDepartments(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> result = apiService.getAllDepartments(page, size);

            model.addAttribute("departments", result.get("departments"));
            model.addAttribute("currentPage", result.get("currentPage"));
            model.addAttribute("totalPages", result.get("totalPages"));
            model.addAttribute("totalElements", result.get("totalElements"));

            if (result.containsKey("error")) {
                model.addAttribute("error", result.get("error"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load departments: " + e.getMessage());
        }

        model.addAttribute("activePage", "departments");

        return "DepartmentAndPhysician/departments";
    }

    @GetMapping("/departments/new")
    public String createDepartmentForm(Model model) {
        model.addAttribute("department", new com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("activePage", "departments");
        return "DepartmentAndPhysician/department-form";
    }

    @PostMapping("/departments/new")
    public String createDepartment(@RequestParam Integer departmentId,
                                   @RequestParam String name,
                                   @RequestParam String headName,
                                   RedirectAttributes redirectAttributes) {
        try {
            com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO dto = com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO.builder()
                    .departmentId(departmentId)
                    .name(name)
                    .headName(headName)
                    .build();
            apiService.createDepartment(dto);
            redirectAttributes.addFlashAttribute("success", "Department created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to create department: " + e.getMessage());
        }
        return "redirect:/departments";
    }

    @GetMapping("/departments/{id}/edit")
    public String editDepartmentForm(@PathVariable Integer id, Model model) {
        try {
            com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO dept = apiService.getDepartmentById(id);
            model.addAttribute("department", dept);
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "departments");
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/departments";
        }
        return "DepartmentAndPhysician/department-form";
    }

    @PostMapping("/departments/{id}/edit")
    public String updateDepartment(@PathVariable Integer id,
                                   @RequestParam String name,
                                   @RequestParam String headName,
                                   RedirectAttributes redirectAttributes) {
        try {
            com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO dto = com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO.builder()
                    .departmentId(id)
                    .name(name)
                    .headName(headName)
                    .build();
            apiService.updateDepartment(id, dto);
            redirectAttributes.addFlashAttribute("success", "Department updated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update department: " + e.getMessage());
        }
        return "redirect:/departments";
    }

    @PostMapping("/departments/{id}/delete")
    public String deleteDepartment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            apiService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("success", "Department deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete department: " + e.getMessage());
        }
        return "redirect:/departments";
    }

    // ─── AFFILIATIONS ───────────────────────────────────────────────────

    @GetMapping("/affiliations")
    public String listAffiliations(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> result = apiService.getAllAffiliations(page, size);

            model.addAttribute("affiliations", result.get("affiliations"));
            model.addAttribute("currentPage", result.get("currentPage"));
            model.addAttribute("totalPages", result.get("totalPages"));
            model.addAttribute("totalElements", result.get("totalElements"));

            if (result.containsKey("error")) {
                model.addAttribute("error", result.get("error"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load affiliations: " + e.getMessage());
        }
        
        model.addAttribute("activePage", "affiliations");

        return "DepartmentAndPhysician/affiliations";
    }

    @GetMapping("/affiliations/new")
    public String createAffiliationForm(Model model,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "100") int size) {
        try {
            Map<String, Object> physiciansResult = apiService.getAllPhysicians(page, size);
            Map<String, Object> departmentsResult = apiService.getAllDepartments(page, size);

            model.addAttribute("physicians", physiciansResult.get("physicians"));
            model.addAttribute("departments", departmentsResult.get("departments"));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load options: " + e.getMessage());
        }
        model.addAttribute("activePage", "affiliations");
        return "DepartmentAndPhysician/affiliation-form";
    }

    @PostMapping("/affiliations/new")
    public String createAffiliation(@RequestParam Integer physicianId,
                                    @RequestParam Integer departmentId,
                                    @RequestParam(defaultValue = "false") Boolean isPrimary,
                                    RedirectAttributes redirectAttributes) {
        try {
            apiService.createAffiliation(physicianId, departmentId, isPrimary);
            redirectAttributes.addFlashAttribute("success", "Affiliation created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to create affiliation: " + e.getMessage());
        }
        return "redirect:/affiliations";
    }

    @PostMapping("/affiliations/delete")
    public String deleteAffiliation(@RequestParam Integer physicianId,
                                    @RequestParam Integer departmentId,
                                    RedirectAttributes redirectAttributes) {
        try {
            apiService.deleteAffiliation(physicianId, departmentId);
            redirectAttributes.addFlashAttribute("success", "Affiliation deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete affiliation: " + e.getMessage());
        }
        return "redirect:/affiliations";
    }
}