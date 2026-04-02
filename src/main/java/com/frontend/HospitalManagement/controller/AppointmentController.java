package com.frontend.HospitalManagement.controller;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.frontend.HospitalManagement.dto.appointment.AppointmentCreateDto;
import com.frontend.HospitalManagement.dto.patient.PatientUpdateDto;
import com.frontend.HospitalManagement.service.PatientService;


@Controller
public class AppointmentController {

    private final PatientService patientService;

    public AppointmentController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/patients/{ssn}/appointments")
    public String viewPatientAppointments(@PathVariable Integer ssn, @RequestParam(defaultValue = "0") int page, Model model) {

    Map<String, Object> data = patientService.getAppointmentsByPatientSsn(ssn, page);
    
    PatientUpdateDto patient = patientService.getPatientBySsn(ssn);
    
    model.addAttribute("appointments", data.get("appointments"));
    model.addAttribute("currentPage", data.get("currentPage"));
    model.addAttribute("totalPages", data.get("totalPages"));
    
    model.addAttribute("patientName", patient.getName());
    model.addAttribute("ssn", ssn);
    
    return "patient/patient-appointments"; 
}

    @GetMapping("/appointments/book")
public String showBookingForm(@RequestParam Integer ssn, Model model) {
    // We need the patient name for the header
    PatientUpdateDto patient = patientService.getPatientBySsn(ssn);
    
    AppointmentCreateDto booking = new AppointmentCreateDto();
    booking.setPatientSsn(ssn);

    model.addAttribute("booking", booking);
    model.addAttribute("patientName", patient.getName());
    return "patient/book-appointment";
}

@PostMapping("/appointments/save")
public String saveAppointment(@ModelAttribute AppointmentCreateDto dto, RedirectAttributes ra) {
    String result = patientService.bookAppointment(dto);
    if ("SUCCESS".equals(result)) {
        ra.addFlashAttribute("successMessage", "Appointment booked successfully!");
    } else {
        ra.addFlashAttribute("errorMessage", result);
    }
    return "redirect:/patients/" + dto.getPatientSsn() + "/appointments";
}
}