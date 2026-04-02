package com.frontend.HospitalManagement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.frontend.HospitalManagement.dto.appointment.AppointmentCreateDto;
import com.frontend.HospitalManagement.dto.appointment.AppointmentDto;
import com.frontend.HospitalManagement.dto.patient.PatientAddDto;
import com.frontend.HospitalManagement.dto.patient.PatientDto;
import com.frontend.HospitalManagement.dto.patient.PatientUpdateDto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;



import java.util.*;

@Service
public class PatientService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public PatientService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    // ── Get all patients with pagination ──────────────────────
    public Map<String, Object> getPatients(int page, int size) {

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/patients")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parsePatientResponse(response);
    }

 public Map<String, Object> searchPatientsByName(String name, int page, int size) {

    try {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/patients/search/findByNameContainingIgnoreCase")
                        .queryParam("name", name)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("SEARCH RESPONSE: " + response); // ← add this
        return parsePatientResponse(response);

    } catch (Exception e) {
        e.printStackTrace(); // ← this will print the real error in console
        Map<String, Object> empty = new HashMap<>();
        empty.put("patients", new ArrayList<>());
        empty.put("currentPage", 0);
        empty.put("totalPages", 0);
        empty.put("totalElements", 0);
        return empty;
    }
}


    // ── Add a new patient ─────────────────────────────────────
    public String addPatient(PatientAddDto dto) {

        if (dto.getPhone() != null && dto.getPhone().replaceAll("\\D", "").length() > 10) {
            return "Phone number exceeds 10 digits";
        }

        try {
            webClient.get().uri("/allPhysician/" + dto.getPcpId())
                    .retrieve().toBodilessEntity().block();
        } catch (Exception e) {
            return "invalid physician which does not exists";
        }

        String requestBody = String.format("""
                {
                    "ssn": %d,
                    "name": "%s",
                    "address": "%s",
                    "phone": "%s",
                    "insuranceID": %d,
                    "pcp": "/allPhysician/%d"
                }
                """,
                dto.getSsn(),
                dto.getName(),
                dto.getAddress(),
                dto.getPhone(),
                dto.getInsuranceID(),
                dto.getPcpId()
        );

        try {
            webClient.post()
                    .uri("/patients")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to add patient. Please check the details and try again.";
        }
    }

    // ── Get single patient by SSN (for edit page pre-fill) ────
    public PatientUpdateDto getPatientBySsn(Integer ssn) {

        String response = webClient.get()
                .uri("/patients/" + ssn)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        PatientUpdateDto dto = new PatientUpdateDto();

        try {
            JsonNode node = objectMapper.readTree(response);
            dto.setName(node.path("name").asString());
            dto.setAddress(node.path("address").asString());
            dto.setPhone(node.path("phone").asString());
            dto.setInsuranceID(node.path("insuranceID").asInt());
            // pcpId left blank — user must re-enter only if changing

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }

    // ── Update an existing patient ────────────────────────────
    public boolean updatePatient(Integer ssn, PatientUpdateDto dto) {

        StringBuilder json = new StringBuilder("{");

        if (dto.getName()        != null && !dto.getName().isEmpty())
            json.append("\"name\": \"").append(dto.getName()).append("\",");
        if (dto.getAddress()     != null && !dto.getAddress().isEmpty())
            json.append("\"address\": \"").append(dto.getAddress()).append("\",");
        if (dto.getPhone()       != null && !dto.getPhone().isEmpty())
            json.append("\"phone\": \"").append(dto.getPhone()).append("\",");
        if (dto.getInsuranceID() != null)
            json.append("\"insuranceID\": ").append(dto.getInsuranceID()).append(",");
        if (dto.getPcpId()       != null)
            json.append("\"pcp\": \"/allPhysician/").append(dto.getPcpId()).append("\",");

        // Remove trailing comma
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("}");

        try {
            webClient.patch()
                    .uri("/patients/" + ssn)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(json.toString())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Shared: Parse _embedded response into patients + page ─
    private Map<String, Object> parsePatientResponse(String response) {

        List<PatientDto> patientList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode patientsNode = root.path("_embedded").path("patients");

            if (patientsNode.isArray()) {
                for (JsonNode node : patientsNode) {
                    PatientDto patient = new PatientDto();

                    // Extract SSN from self href, fallback to direct field
                    String selfHref = node.path("_links").path("self").path("href").asString("");
                    if (!selfHref.isEmpty()) {
                        String cleanHref = selfHref.split("\\?")[0];
                        String ssnStr = cleanHref.substring(cleanHref.lastIndexOf('/') + 1);
                        try {
                            patient.setSsn(Integer.parseInt(ssnStr));
                        } catch (NumberFormatException e) {
                            patient.setSsn(node.path("ssn").asInt());
                        }
                    } else {
                        patient.setSsn(node.path("ssn").asInt());
                    }

                    patient.setName(node.path("name").asString());
                    patient.setAddress(node.path("address").asString());
                    patient.setPhone(node.path("phone").asString());

                    String pcpName = node.path("pcpName").asString("");
                    if (pcpName.isEmpty()) {
                        pcpName = node.path("pcp").path("name").asString("Unknown");
                    }
                    patient.setPcpName(pcpName);

                    patientList.add(patient);
                }
            }

            JsonNode pageNode = root.path("page");
            result.put("currentPage",   pageNode.path("number").asInt());
            result.put("totalPages",    pageNode.path("totalPages").asInt());
            result.put("totalElements", pageNode.path("totalElements").asInt());
            result.put("patients",      patientList);

        } catch (Exception e) {
            e.printStackTrace();
            // Return empty result instead of crashing
            result.put("currentPage",   0);
            result.put("totalPages",    0);
            result.put("totalElements", 0);
            result.put("patients",      patientList);
        }

        return result;
    }

    // ── Get appointments by patient SSN with pagination ───────
    public Map<String, Object> getAppointmentsByPatientSsn(Integer ssn, int page) {

        List<AppointmentDto> appointmentList = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/patients/" + ssn + "/appointments")
                            .queryParam("page", page)
                            .queryParam("size", 5)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);

            // Parse appointments
            JsonNode appointmentsNode = root.path("_embedded").path("appointments");
            if (appointmentsNode.isArray()) {
                for (JsonNode node : appointmentsNode) {
                    AppointmentDto dto = new AppointmentDto();
                    dto.setAppointmentId(node.path("appointmentId").asInt());

                    // Date formatting: replace T separator and trim to minute precision
                    String rawStart = node.path("starto").asString();
                    String rawEnd   = node.path("endo").asString();
                    dto.setStarto(rawStart != null && rawStart.contains("T")
                            ? rawStart.replace("T", " ").substring(0, 16) : rawStart);
                    dto.setEndo(rawEnd != null && rawEnd.contains("T")
                            ? rawEnd.replace("T", " ").substring(0, 16) : rawEnd);

                    dto.setExaminationRoom(node.path("examinationRoom").asString());
                    dto.setPhysicianName(node.path("physicianName")
                            .asString(node.path("physician").path("name").asString("Unknown")));

                    appointmentList.add(dto);
                }
            }

            // Parse pagination info
            JsonNode pageNode = root.path("page");
            result.put("appointments", appointmentList);
            result.put("currentPage",  pageNode.path("number").asInt(0));
            result.put("totalPages",   pageNode.path("totalPages").asInt(1));

        } catch (Exception e) {
            e.printStackTrace();
            result.put("appointments", new ArrayList<>());
            result.put("currentPage",  0);
            result.put("totalPages",   1);
        }

        return result;
    }

    // ── Book a new appointment ────────────────────────────────
    public String bookAppointment(AppointmentCreateDto dto) {

        // Validate physician exists
        try {
            webClient.get()
                    .uri("/allPhysician/" + dto.getPhysicianId())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            return "select from existing physician and nurse";
        }

        // Validate nurse exists (if provided)
        if (dto.getNurseId() != null) {
            try {
                webClient.get()
                        .uri("/nurses/" + dto.getNurseId())
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            } catch (Exception e) {
                return "select from existing physician and nurse";
            }
        }

        String json = String.format("""
                {
                    "appointmentId": %d,
                    "patient": "/patients/%d",
                    "physician": "/allPhysician/%d",
                    "prepNurse": %s,
                    "starto": "%s:00",
                    "endo": "%s:00",
                    "examinationRoom": "%s"
                }
                """,
                dto.getAppointmentId(),
                dto.getPatientSsn(),
                dto.getPhysicianId(),
                (dto.getNurseId() != null) ? "\"/nurses/" + dto.getNurseId() + "\"" : "null",
                dto.getStarto(),
                dto.getEndo(),
                dto.getExaminationRoom()
        );

        try {
            webClient.post()
                    .uri("/appointments")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to book appointment. Please check IDs.";
        }
    }
}