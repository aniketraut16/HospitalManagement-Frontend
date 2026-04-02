package com.frontend.HospitalManagement.service.Medication;


import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.frontend.HospitalManagement.dto.Medication.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final RestTemplate restTemplate;

    @Value("${spring.base}")
    String baseUrl;


    public List<MedicationDTO> getAllMedications(int page, int size) {

        String url = baseUrl + "/allMedications?page=" + page + "&size=" + size;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");

        List<Map<String, Object>> list =
                (List<Map<String, Object>>) embedded.get("medications");

        return list.stream().map(m -> {
            MedicationDTO dto = new MedicationDTO();
            dto.setCode((Integer) m.get("id"));
            dto.setName((String) m.get("name"));
            dto.setBrand((String) m.get("brand"));
            dto.setDescription((String) m.get("description"));
            return dto;
        }).toList();
    }

    // returning full response
    public Map<String, Object> getMedications(int page, int size) {

        String url = baseUrl + "/allMedications?page=" + page + "&size=" + size;

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        Map<String, Object> embedded =
                (Map<String, Object>) response.get("_embedded");

        List<Map<String, Object>> list =
                (List<Map<String, Object>>) embedded.get("medications");

        List<MedicationDTO> medications = list.stream().map(m -> {
            MedicationDTO dto = new MedicationDTO();
            dto.setCode((Integer) m.get("id"));
            dto.setName((String) m.get("name"));
            dto.setBrand((String) m.get("brand"));
            dto.setDescription((String) m.get("description"));
            return dto;
        }).toList();

        Map<String, Object> pageInfo =
                (Map<String, Object>) response.get("page");

        Map<String, Object> result = new HashMap<>();
        result.put("data", medications);
        result.put("page", pageInfo);

        return result;
    }

    // search service
    public Map<String, Object> searchMedications(String keyword, int page, int size) {

        String url = baseUrl +
                "/allMedications/search/findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrDescriptionContainingIgnoreCase" +
                "?name=" + keyword +
                "&brand=" + keyword +
                "&description=" + keyword +
                "&page=" + page +
                "&size=" + size;

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        Map<String, Object> embedded =
                (Map<String, Object>) response.get("_embedded");

        List<Map<String, Object>> list =
                (List<Map<String, Object>>) embedded.get("medications");

        List<MedicationDTO> medications = list.stream().map(m -> {
            MedicationDTO dto = new MedicationDTO();
            dto.setCode((Integer) m.get("id"));
            dto.setName((String) m.get("name"));
            dto.setBrand((String) m.get("brand"));
            dto.setDescription((String) m.get("description"));
            return dto;
        }).toList();

        Map<String, Object> pageInfo =
                (Map<String, Object>) response.get("page");

        Map<String, Object> result = new HashMap<>();
        result.put("data", medications);
        result.put("page", pageInfo);
        return result;
    }

    // GET BY ID
    public MedicationDTO getMedicationById(int id) {

        String url = baseUrl + "/allMedications/" + id;

        Map<String, Object> m = restTemplate.getForObject(url, Map.class);

        MedicationDTO dto = new MedicationDTO();
        dto.setCode((Integer) m.get("id"));
        dto.setName((String) m.get("name"));
        dto.setBrand((String) m.get("brand"));
        dto.setDescription((String) m.get("description"));

        return dto;
    }

    public String createMedication(MedicationDTO medication) {
        try {
            String url = baseUrl + "/allMedications";

            Map<String, Object> body = new HashMap<>();
            body.put("code", medication.getCode());
            body.put("name", medication.getName());
            body.put("brand", medication.getBrand());
            body.put("description", medication.getDescription());

            restTemplate.postForObject(url, body, Object.class);
            return null;

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String updateMedication(MedicationDTO medication) {
        try {
            String url = baseUrl + "/allMedications/" + medication.getCode();

            Map<String, Object> body = new HashMap<>();
            body.put("name", medication.getName());
            body.put("brand", medication.getBrand());
            body.put("description", medication.getDescription());

            restTemplate.put(url, body);
            return null;

        } catch (Exception e) {
            return e.getMessage();
        }
    }


    public String getMedicationNameById(int id) {

        String url = baseUrl + "/medications/" + id;

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        return (String) response.get("name");
    }


}
