package com.frontend.HospitalManagement.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.frontend.HospitalManagement.dto.Procedure.ProcedureDto;
import com.frontend.HospitalManagement.dto.Procedure.ProcedureResponse;
import com.frontend.HospitalManagement.dto.TrainedIn.TrainedInResponse;
import com.frontend.HospitalManagement.service.ProcedureService;

@Controller
public class ProcedureAndCertificationController {

    private final ProcedureService apiService;

    public ProcedureAndCertificationController(ProcedureService apiService) {
        this.apiService = apiService;
    }

@GetMapping("/procedures")
public String getProcedures(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "") String message,
        Model model) {

    ProcedureResponse response;
    try {
        response = search.isBlank()
                ? apiService.getProcedures(page, size)
                : apiService.searchByName(search, page, size);
    } catch (Exception e) {
        e.printStackTrace();
        response = null;
    }

    boolean hasResults = response != null
            && response.getEmbedded() != null
            && response.getEmbedded().getProcedures() != null
            && !response.getEmbedded().getProcedures().isEmpty();

    boolean hasPage = response != null && response.getPage() != null;

    if (hasResults) {
        model.addAttribute("procedures", response.getEmbedded().getProcedures());
        model.addAttribute("currentPage", hasPage ? response.getPage().getNumber() : page);
        model.addAttribute("totalPages", hasPage ? response.getPage().getTotalPages() : 1);
        model.addAttribute("totalElements", hasPage ? response.getPage().getTotalElements() : response.getEmbedded().getProcedures().size());
    } else {
        model.addAttribute("procedures", java.util.List.of());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("totalElements", 0);
    }

    model.addAttribute("size", size);
    model.addAttribute("searchPerformed", !search.isBlank());
    model.addAttribute("search", search);
    model.addAttribute("message", message);
    return "ProcedureAndCertifications/procedures";
}
@PostMapping("/procedures/add")
public String addProcedure(@ModelAttribute ProcedureDto procedure) {
    if (procedure.getName() == null || procedure.getName().trim().isBlank()) {
        return "redirect:/procedures?message=Procedure+name+cannot+be+blank";
    }
    procedure.setName(procedure.getName().trim());

    try {
        apiService.addProcedure(procedure);
        return "redirect:/procedures?message=Procedure+added+successfully";
    } catch (Exception e) {
        return "redirect:/procedures?message=Failed+to+add+procedure";
    }
}

    @PostMapping("/procedures/update")
public String updateProcedure(
        @RequestParam Integer code,
        @RequestParam Double cost) {
    apiService.updateProcedureCost(code, cost);
    return "redirect:/procedures?message=Cost+updated+successfully";
}

@GetMapping("/procedures/{code}/trainedIn")
public String getTrainedIn(
        @PathVariable Integer code,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "") String procedureName,
        @RequestParam(defaultValue = "") String message,   // ← add this
        Model model) {

    TrainedInResponse response = apiService.getTrainedInByProcedure(code, page, size);

    if (response != null && response.getEmbedded() != null) {
        model.addAttribute("trainedIns", response.getEmbedded().getTrainedIns());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", response.getPage().getTotalPages());
        model.addAttribute("totalElements", response.getPage().getTotalElements());
    } else {
        model.addAttribute("trainedIns", java.util.List.of());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        model.addAttribute("totalElements", 0);
    }

    model.addAttribute("code", code);
    model.addAttribute("procedureName", procedureName);
    model.addAttribute("size", size);
    model.addAttribute("message", message);   // ← add this

    return "ProcedureAndCertifications/trainedIn";
}

@PostMapping("/trainedIn/renew") // Combined with @RequestMapping("/api"), this is /api/trainedIn/renew
public String processRenewal(
        @RequestParam("treatmentId") Integer treatmentId, // Added explicit names just to be safe
        @RequestParam("physicianId") Integer physicianId,
        @RequestParam("certDate") String certDate,
        @RequestParam("expDate") String expDate,
        @RequestParam("procedureName") String procedureName) {

String formattedCert = certDate + "T00:00:00.000Z";
String formattedExp  = expDate  + "T00:00:00.000Z";

    apiService.renewTrainedIn(treatmentId, physicianId, formattedCert, formattedExp);

    return "redirect:/procedures/" + treatmentId + "/trainedIn?procedureName=" + procedureName;
}

@PostMapping("/trainedIn/add")
public String addCertification(
        @RequestParam Integer treatmentId,
        @RequestParam Integer physicianId,
        @RequestParam String certDate,
        @RequestParam String expDate,
        @RequestParam String procedureName) {

    String formattedCert = certDate + "T00:00:00.000Z";
    String formattedExp  = expDate  + "T00:00:00.000Z";

    try {
        apiService.addTrainedIn(treatmentId, physicianId, formattedCert, formattedExp);
        return "redirect:/procedures/" + treatmentId + "/trainedIn"
                + "?procedureName=" + procedureName
                + "&message=Certification+added+successfully";
    } catch (Exception e) {
        String msg = e.getMessage() != null && e.getMessage().contains("duplicate")
                ? "duplicate+entry+—+this+physician+is+already+certified+for+this+procedure"
                : "Physician+not+found+or+invalid+input";
        return "redirect:/procedures/" + treatmentId + "/trainedIn"
                + "?procedureName=" + procedureName
                + "&message=" + msg;
    }
}


}
