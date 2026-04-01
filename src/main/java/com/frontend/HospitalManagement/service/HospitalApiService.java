package com.frontend.HospitalManagement.service;

import com.frontend.HospitalManagement.dto.Affiliatedto.AffiliationDTO;
import com.frontend.HospitalManagement.dto.Physiciandto.PhysicianDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class HospitalApiService {

    @Value("${spring.base}")
    private String backendUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HospitalApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ─── PHYSICIAN ENDPOINTS ────────────────────────────────────────────

    public Map<String, Object> getAllPhysicians(int page, int size) {
        String url = backendUrl + "/allPhysician?page=" + page + "&size=" + size;
        return fetchPhysicianPage(url);
    }

    public Map<String, Object> searchPhysiciansByName(String name, int page, int size) {
        String url = backendUrl + "/allPhysician/search/findByNameContaining?name=" + name + "&page=" + page + "&size="
                + size;
        return fetchPhysicianPage(url);
    }

    public Map<String, Object> searchPhysiciansByPosition(String position, int page, int size) {
        String url = backendUrl + "/allPhysician/search/findByPositionContaining?position=" + position + "&page=" + page
                + "&size=" + size;
        return fetchPhysicianPage(url);
    }

    public Map<String, Object> searchPhysicianBySsn(int ssn) {
        String url = backendUrl + "/allPhysician/search/findBySsnContaining?ssn=" + ssn + "&page=0&size=10";
        return fetchPhysicianPage(url);
    }

    public PhysicianDTO getPhysicianById(Integer id) {
        String url = backendUrl + "/allPhysician/" + id;
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);
            return mapToPhysicianDTO(node, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch physician with ID: " + id, e);
        }
    }

    public void createPhysician(PhysicianDTO dto) {
        String url = backendUrl + "/allPhysician";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("employeeId", dto.getEmployeeId());
        body.put("name", dto.getName());
        body.put("position", dto.getPosition());
        body.put("ssn", dto.getSsn());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    public void updatePhysician(Integer id, PhysicianDTO dto) {
        String url = backendUrl + "/allPhysician/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("employeeId", id);
        body.put("name", dto.getName());
        body.put("position", dto.getPosition());
        body.put("ssn", dto.getSsn());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.put(url, request);
    }

    public void deletePhysician(Integer id) {
        String url = backendUrl + "/allPhysician/" + id;
        restTemplate.delete(url);
    }

    // ─── AFFILIATION ENDPOINTS ──────────────────────────────────────────

    public Map<String, Object> getAllAffiliations(int page, int size) {
        String url = backendUrl + "/affiliations?page=" + page + "&size=" + size + "&projection=fullAffiliation";
        return fetchAffiliationPage(url);
    }

    public List<AffiliationDTO> getAffiliationsByPhysician(Integer physicianId) {
        String url = backendUrl + "/affiliations/search/findByPhysician?physicianId=" + physicianId
                + "&projection=fullAffiliation";
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedded = root.path("_embedded").path("affiliations");

            List<AffiliationDTO> affiliations = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    affiliations.add(mapToAffiliationDTO(node));
                }
            }
            return affiliations;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public void createAffiliation(Integer physicianId, Integer departmentId, Boolean isPrimary) {
        String url = backendUrl + "/affiliations";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("primaryAffiliation", isPrimary);
        body.put("physician", backendUrl + "/allPhysician/" + physicianId);
        body.put("department", backendUrl + "/departments/" + departmentId);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    public void deleteAffiliation(Integer physicianId, Integer departmentId) {
        // Find affiliation to delete
        String url = backendUrl + "/affiliations/search/findByPhysician?physicianId=" + physicianId;
        try {
            // In a perfect REST API, there'd be a direct ID or composite ID.
            // We'll rely on the backend handling the delete mapped correctly if we had ID.
            // Usually it's better to fetch and delete by href.
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedded = root.path("_embedded").path("affiliations");
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    String deptName = node.path("departmentName").asText("");
                    // Since we only have departmentId, we might need a different search or fetch
                    // department first.
                    // A simplified deletion:
                    String selfHref = node.path("_links").path("self").path("href").asText("");
                    // Here we'd need to match the department ID if the projection returns it.
                    // Assuming the backend has a custom endpoint:
                    restTemplate.delete(backendUrl + "/affiliations/delete?physicianId=" + physicianId
                            + "&departmentId=" + departmentId);
                    return;
                }
            }
        } catch (Exception e) {
        }
    }

    // ─── DEPARTMENT ENDPOINTS ───────────────────────────────────────────

    public Map<String, Object> getAllDepartments(int page, int size) {
        String url = backendUrl + "/departments?page=" + page + "&size=" + size;
        return fetchDepartmentPage(url);
    }

    public com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO getDepartmentById(Integer id) {
        String url = backendUrl + "/departments/" + id;
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(response);
            return mapToDepartmentDTO(node, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch department with ID: " + id, e);
        }
    }

    public void createDepartment(com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO dto) {
        String url = backendUrl + "/departments";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("departmentId", dto.getDepartmentId());
        body.put("name", dto.getName());
        body.put("headName", dto.getHeadName());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    public void updateDepartment(Integer id, com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO dto) {
        String url = backendUrl + "/departments/" + id;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("departmentId", id);
        body.put("name", dto.getName());
        body.put("headName", dto.getHeadName());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.put(url, request);
    }

    public void deleteDepartment(Integer id) {
        String url = backendUrl + "/departments/" + id;
        restTemplate.delete(url);
    }

    // ─── PRIVATE HELPERS ────────────────────────────────────────────────

    private Map<String, Object> fetchPhysicianPage(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedded = root.path("_embedded").path("physicians");

            List<PhysicianDTO> physicians = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    Integer id = null;
                    if (node.has("employeeId")) {
                        id = node.get("employeeId").asInt();
                    } else {
                        String selfHref = node.path("_links").path("self").path("href").asText("");
                        id = extractIdFromHref(selfHref);
                    }
                    physicians.add(mapToPhysicianDTO(node, id));
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("physicians", physicians);
            result.put("currentPage", root.path("page").path("number").asInt(0));
            result.put("totalPages", root.path("page").path("totalPages").asInt(1));
            result.put("totalElements", root.path("page").path("totalElements").asLong(0));
            return result;
        } catch (Exception e) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("physicians", Collections.emptyList());
            empty.put("currentPage", 0);
            empty.put("totalPages", 1);
            empty.put("totalElements", 0L);
            empty.put("error", "Unable to connect to backend: " + e.getMessage());
            return empty;
        }
    }

    private Map<String, Object> fetchDepartmentPage(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedded = root.path("_embedded").path("departments");

            List<com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO> departments = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    Integer id = null;
                    if (node.has("departmentId")) {
                        id = node.get("departmentId").asInt();
                    } else {
                        String selfHref = node.path("_links").path("self").path("href").asText("");
                        id = extractIdFromHref(selfHref);
                    }
                    departments.add(mapToDepartmentDTO(node, id));
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("departments", departments);
            result.put("currentPage", root.path("page").path("number").asInt(0));
            result.put("totalPages", root.path("page").path("totalPages").asInt(1));
            result.put("totalElements", root.path("page").path("totalElements").asLong(0));
            return result;
        } catch (Exception e) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("departments", Collections.emptyList());
            empty.put("currentPage", 0);
            empty.put("totalPages", 1);
            empty.put("totalElements", 0L);
            empty.put("error", "Unable to connect to backend: " + e.getMessage());
            return empty;
        }
    }

    private Map<String, Object> fetchAffiliationPage(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode embedded = root.path("_embedded").path("affiliations");

            List<AffiliationDTO> affiliations = new ArrayList<>();
            if (embedded.isArray()) {
                for (JsonNode node : embedded) {
                    affiliations.add(mapToAffiliationDTO(node));
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("affiliations", affiliations);
            result.put("currentPage", root.path("page").path("number").asInt(0));
            result.put("totalPages", root.path("page").path("totalPages").asInt(1));
            result.put("totalElements", root.path("page").path("totalElements").asLong(0));
            return result;
        } catch (Exception e) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("affiliations", Collections.emptyList());
            empty.put("currentPage", 0);
            empty.put("totalPages", 1);
            empty.put("totalElements", 0L);
            empty.put("error", "Unable to connect to backend: " + e.getMessage());
            return empty;
        }
    }

    private PhysicianDTO mapToPhysicianDTO(JsonNode node, Integer fallbackId) {
        Integer id = fallbackId;
        if (node.has("employeeId")) {
            id = node.get("employeeId").asInt();
        }
        return PhysicianDTO.builder()
                .employeeId(id)
                .name(node.path("name").asText(""))
                .position(node.path("position").asText(""))
                .ssn(node.has("ssn") ? node.get("ssn").asInt() : null)
                .build();
    }

    private com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO mapToDepartmentDTO(JsonNode node,
            Integer fallbackId) {
        Integer id = fallbackId;
        if (node.has("departmentId")) {
            id = node.get("departmentId").asInt();
        }
        return com.frontend.HospitalManagement.dto.Departmentdto.DepartmentDTO.builder()
                .departmentId(id)
                .name(node.path("name").asText(""))
                .headName(node.path("headName").asText(""))
                .build();
    }

    private AffiliationDTO mapToAffiliationDTO(JsonNode node) {
        return AffiliationDTO.builder()
                .primaryAffiliation(node.path("primaryAffiliation").asBoolean())
                .doctorName(node.path("doctorName").asText(""))
                .doctorPosition(node.path("doctorPosition").asText(""))
                .departmentName(node.path("departmentName").asText(""))
                .departmentHeadName(node.path("departmentHeadName").asText("N/A"))
                .build();
    }

    private Integer extractIdFromHref(String href) {
        if (href == null || href.isEmpty())
            return null;
        String[] parts = href.split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
