package com.frontend.HospitalManagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.HospitalManagement.dto.Nurse.NurseDTO;
import com.frontend.HospitalManagement.dto.Nurse.NursePageResponse;
import com.frontend.HospitalManagement.dto.Nurse.NursePosition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class NurseApiService {

    @Autowired
    private WebClient webClient;

    public NurseApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String call(String url, HttpMethod method, Object body) {

        WebClient.RequestBodySpec request = webClient
                .method(method)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            return request.bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }

        return request.retrieve()
                .bodyToMono(String.class)
                .block();
    }


    public NursePageResponse getNurses(int page, int size, String keyword, NursePosition positionFilter) {

        String json = call("/nurse?projection=nurseView&page=0&size=1000", HttpMethod.GET, null);

        List<NurseDTO> allNurses = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);

            JsonNode embedded = root.path("_embedded");
            if (embedded.isMissingNode()) {
                return new NursePageResponse();
            }

            JsonNode nurses = embedded.path("nurses");
            if (!nurses.isArray()) {
                return new NursePageResponse();
            }

            for (JsonNode node : nurses) {

                if (node == null || node.isNull()) continue;

                NurseDTO dto = new NurseDTO();

                JsonNode linkNode = node.path("_links").path("self").path("href");
                if (!linkNode.isMissingNode()) {
                    String href = linkNode.asText();
                    if (href != null && href.contains("/")) {
                        try {
                            Integer id = Integer.parseInt(href.substring(href.lastIndexOf("/") + 1));
                            dto.setEmployeeId(id);
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }

                if (dto.getEmployeeId() == null) continue;

                dto.setName(node.path("name").asText("N/A"));

                String positionText = node.path("position").asText("");
                NursePosition position = NursePosition.fromDisplayName(positionText);
                dto.setPosition(position);

                dto.setRegistered(node.path("registered").asBoolean(false));
                dto.setAvailability(node.path("availability").asText("UNKNOWN"));

                allNurses.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parsing nurse data", e);
        }

        if (keyword != null && !keyword.isBlank()) {
            String lower = keyword.toLowerCase();
            allNurses = allNurses.stream()
                    .filter(n ->
                            (n.getName() != null && n.getName().toLowerCase().contains(lower)) ||
                            (n.getPosition() != null && n.getPosition().getDisplayName().toLowerCase().contains(lower))
                    )
                    .toList();
        }

        if (positionFilter != null) {
            allNurses = allNurses.stream()
                    .filter(n -> positionFilter.equals(n.getPosition()))
                    .toList();
        }

        int totalElements = allNurses.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<NurseDTO> pageList = new ArrayList<>();
        if (start < totalElements) {
            pageList = allNurses.subList(start, end);
        }

        NursePageResponse result = new NursePageResponse();
        result.setNurses(pageList);
        result.setTotalPages(totalPages);

        return result;
    }

    public void addNurse(NurseDTO nurse) {

        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", nurse.getEmployeeId());
        request.put("name", nurse.getName());
        request.put("position", nurse.getPosition() != null ? nurse.getPosition().getDisplayName() : null);
        request.put("registered", nurse.isRegistered());
        request.put("ssn", nurse.getSsn());

        webClient.post()
                .uri("/nurse")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void updateNurse(Integer id, NurseDTO nurse) {

        Map<String, Object> request = new HashMap<>();
        request.put("name", nurse.getName());
        request.put("position", nurse.getPosition() != null ? nurse.getPosition().getDisplayName() : null);
        request.put("registered", nurse.isRegistered());
        request.put("ssn", nurse.getSsn());

        webClient.put()
                .uri("/nurse/" + id)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public NurseDTO getNurseById(Integer id) {

        Map<String, Object> response = webClient.get()
                .uri("/nurse/" + id + "?projection=nurseView")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        NurseDTO dto = new NurseDTO();
        dto.setEmployeeId(id);
        dto.setName((String) response.get("name"));

        String positionText = (String) response.get("position");
        dto.setPosition(NursePosition.fromDisplayName(positionText));

        dto.setRegistered((Boolean) response.get("registered"));

        Object ssnRaw = response.get("ssn");
        if (ssnRaw instanceof Integer) {
            dto.setSsn((Integer) ssnRaw);
        } else if (ssnRaw instanceof Number) {
            dto.setSsn(((Number) ssnRaw).intValue());
        }

        return dto;
    }

    public Map<String, Object> getAppointmentByNurse(Integer nurseId) {

        String uri = "/appointments/search/byNurse?nurse=http://localhost:9090/nurse/" + nurseId + "&projection=appointmentView";

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        if (embedded == null || embedded.get("appointments") == null) return null;

        List<Map<String, Object>> list = (List<Map<String, Object>>) embedded.get("appointments");
        if (list.isEmpty()) return null;

        return list.get(0);
    }

    public Map<String, Object> getOnCallByNurse(Integer nurseId) {

        String uri = "/oncalls/search/byNurse?nurse=" + nurseId + "&projection=onCallView";

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) return null;
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        if (embedded == null) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) embedded.get("onCalls");
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    public String getNurseStatus(Integer nurseId) {
        Map<String, Object> appointment = getAppointmentByNurse(nurseId);
        if (appointment != null) {
            return "BUSY";
        }
        Map<String, Object> onCall = getOnCallByNurse(nurseId);
        if (onCall != null) {
            return "ON CALL";
        }
        return "AVAILABLE";
    }
}
