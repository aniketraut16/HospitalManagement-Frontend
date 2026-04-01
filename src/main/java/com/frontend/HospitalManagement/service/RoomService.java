package com.frontend.HospitalManagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.HospitalManagement.dto.room.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class RoomService {
    

    @Autowired
    private WebClient webClient;

    public  Mono<String> call(String url, HttpMethod method, Object body) {
        WebClient.RequestBodySpec requestSpec = webClient
                .method(method)
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON);

        if (body != null) {
            return requestSpec
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class);
        }

        return requestSpec
                .retrieve()
                .bodyToMono(String.class);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    public StayResponseDto getStaysByRoomNumber(int roomNumber) {
        String url =  "/stays/search/findByRoom_RoomNumber?roomNumber=" + roomNumber;

        String json = call(url, HttpMethod.GET, null).block();

        try {
            JsonNode root = objectMapper.readTree(json);

            // Parse stays from _embedded.stays
            List<StayDto> stays = new ArrayList<>();
            JsonNode staysNode = root.path("_embedded").path("stays");
            for (JsonNode stayNode : staysNode) {
                StayDto stay = new StayDto();
                stay.setStatus(stayNode.path("status").asText());
                stay.setPatientName(stayNode.path("patientName").asText());
                stay.setStayStart(stayNode.path("stayStart").asText());
                stay.setStayEnd(stayNode.path("stayEnd").asText());
                stays.add(stay);
            }

            // Parse page metadata
            PageDto page = new PageDto();
            JsonNode pageNode = root.path("page");
            page.setSize(pageNode.path("size").asInt());
            page.setTotalElements(pageNode.path("totalElements").asLong());
            page.setTotalPages(pageNode.path("totalPages").asInt());
            page.setNumber(pageNode.path("number").asInt());

            return new StayResponseDto(stays, page);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse stays response", e);
        }
    }

    public RoomResponseDto getRoomsByType(String type, int pageNumber) {
        String url =  "/rooms/search/findByRoomType?type=" + type + "&page=" + pageNumber + "&size=8";

        String json = call(url, HttpMethod.GET, null).block();

        try {
            JsonNode root = objectMapper.readTree(json);

            // Parse rooms from _embedded.rooms
            List<RoomDto> rooms = new ArrayList<>();
            JsonNode roomsNode = root.path("_embedded").path("rooms");
            for (JsonNode roomNode : roomsNode) {
                RoomDto room = new RoomDto();
                room.setRoomNumber(roomNode.path("roomNumber").asInt());
                room.setBlock(roomNode.path("block").asText());
                room.setRoomType(roomNode.path("roomType").asText());
                room.setStatus(roomNode.path("status").asText());
                room.setUnavailable(roomNode.path("unavailable").asBoolean());
                rooms.add(room);
            }

            // Parse page metadata
            PageDto page = new PageDto();
            JsonNode pageNode = root.path("page");
            page.setSize(pageNode.path("size").asInt());
            page.setTotalElements(pageNode.path("totalElements").asLong());
            page.setTotalPages(pageNode.path("totalPages").asInt());
            page.setNumber(pageNode.path("number").asInt());

            return new RoomResponseDto(rooms, page);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse rooms response", e);
        }
    }

    public RoomDto getRoomByNumber(int roomNumber) {
        String url =  "/rooms/" + roomNumber;
        String json = call(url, HttpMethod.GET, null).block();
        try {
            JsonNode roomNode = objectMapper.readTree(json);
            RoomDto room = new RoomDto();
            room.setRoomNumber(roomNode.path("roomNumber").asInt());
            room.setBlock(roomNode.path("block").asText(null)); // Might be null
            if (roomNode.has("blockFloor") && roomNode.has("blockCode")) {
                room.setBlockFloor(roomNode.path("blockFloor").asInt());
                room.setBlockCode(roomNode.path("blockCode").asInt());
            }
            room.setRoomType(roomNode.path("roomType").asText());
            room.setStatus(roomNode.path("status").asText());
            room.setUnavailable(roomNode.path("unavailable").asBoolean());
            return room;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse room response", e);
        }
    }

    public RoomActionResponseDTO createRoom(CreateRoomDTO body){
        String url =  "/rooms";
        
        try {
            call(url, HttpMethod.POST, body).block();
            return new RoomActionResponseDTO(true, "Room created successfully");
        } catch (WebClientResponseException e) {
            String message = e.getMessage();
            try {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody != null && !responseBody.isEmpty()) {
                    JsonNode errorNode = objectMapper.readTree(responseBody);
                    if (errorNode.has("cause")) {
                        message = errorNode.get("cause").asText();
                    } else if (errorNode.has("detail")) {
                        message = errorNode.get("detail").asText();
                    }
                }
            } catch (Exception parseException) {
                // Ignore parse exception, fallback to default message
            }
            return new RoomActionResponseDTO(false, message);
        } catch (Exception e) {
            return new RoomActionResponseDTO(false, "An unexpected error occurred: " + e.getMessage());
        }
    }

    public RoomActionResponseDTO toggleUnavailability(UpdateRoomUnavailabilityDTO newUnavailability, Integer roomNumber) {
        String url =  "/rooms/" + roomNumber;
        
        try {
            call(url, HttpMethod.PATCH, newUnavailability).block();
            return new RoomActionResponseDTO(true, "Room unavailability toggled successfully");
        } catch (WebClientResponseException e) {
            return new RoomActionResponseDTO(false, parseErrorMessage(e));
        } catch (Exception e) {
            return new RoomActionResponseDTO(false, "An unexpected error occurred: " + e.getMessage());
        }
    }

    public RoomActionResponseDTO updateRoom(UpdateRoomDTO updates, Integer roomNumber) {
        String url =  "/rooms/" + roomNumber;
        
        try {
            call(url, HttpMethod.PATCH, updates).block();
            return new RoomActionResponseDTO(true, "Room updated successfully");
        } catch (WebClientResponseException e) {
            return new RoomActionResponseDTO(false, parseErrorMessage(e));
        } catch (Exception e) {
            return new RoomActionResponseDTO(false, "An unexpected error occurred: " + e.getMessage());
        }
    }

    private String parseErrorMessage(WebClientResponseException e) {
        String message = e.getMessage();
        try {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                JsonNode errorNode = objectMapper.readTree(responseBody);
                if (errorNode.has("cause")) {
                    message = errorNode.get("cause").asText();
                } else if (errorNode.has("detail")) {
                    message = errorNode.get("detail").asText();
                }
            }
        } catch (Exception parseException) {
            // Ignore parse exception, fallback to default message
        }
        return message;
    }
}
