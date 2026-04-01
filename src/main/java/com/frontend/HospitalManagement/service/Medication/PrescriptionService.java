package com.frontend.HospitalManagement.service.Medication;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.frontend.HospitalManagement.dto.Medication.PrescriptionDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final RestTemplate restTemplate;

    @Value("${spring.base}")
    String baseUrl;

    private String getPhysicianName(Integer id) {
        try {
            Map<String, Object> res =
                    restTemplate.getForObject(baseUrl + "/physicians/" + id, Map.class);

            return (String) res.get("name"); // adjust field if needed
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String getPatientName(Integer id) {
        try {
            Map<String, Object> res =
                    restTemplate.getForObject(baseUrl + "/patients/" + id, Map.class);

            return (String) res.get("name"); // adjust field if needed
        } catch (Exception e) {
            return "Unknown";
        }
    }


    public Map<String, Object> getPrescriptionsByMedication(int medicationId, int page, int size) {

        String url = baseUrl + "/prescribes/search/findByMedication?medication="
                + medicationId + "&page=" + page + "&size=" + size;

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        Map<String, Object> embedded =
                (Map<String, Object>) response.get("_embedded");

        List<Map<String, Object>> list =
                (List<Map<String, Object>>) embedded.get("prescriptions");

        List<PrescriptionDTO> prescriptions = list.stream().map(p -> {

            PrescriptionDTO dto = new PrescriptionDTO();

            dto.setDose((String) p.get("dose"));

            Map<String, Object> innerEmbedded =
                    (Map<String, Object>) p.get("_embedded");

            if (innerEmbedded != null) {

                Map<String, Object> patient =
                        (Map<String, Object>) innerEmbedded.get("patientEntity");

                if (patient != null) {
                    dto.setPatientName((String) patient.get("name"));
                }

                Map<String, Object> appointment =
                        (Map<String, Object>) innerEmbedded.get("appointmentEntity");

                if (appointment != null) {
                    dto.setPhysicianName((String) appointment.get("physicianName"));

                    String date = (String) appointment.get("starto");
                    if (date != null) {
                        dto.setDate(date.substring(0, 10));
                    }
                }
            }

            return dto;

        }).toList();

    Map<String, Object> pageInfo =
            (Map<String, Object>) response.get("page");

    Map<String, Object> result = new HashMap<>();
    result.put("data", prescriptions);
    result.put("page", pageInfo);

    return result;
}

    private int extractId(String url) {
        return Integer.parseInt(url.substring(url.lastIndexOf("/") + 1));
    }
}
