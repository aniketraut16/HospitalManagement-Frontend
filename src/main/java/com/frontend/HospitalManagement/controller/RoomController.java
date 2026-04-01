package com.frontend.HospitalManagement.controller;

import com.frontend.HospitalManagement.dto.room.*;
import com.frontend.HospitalManagement.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/stays/{room_number}")
    public String getStaysByRoom(@PathVariable("room_number") int roomNumber, Model model) {
        StayResponseDto stayResponse = roomService.getStaysByRoomNumber(roomNumber);
        model.addAttribute("stays", stayResponse.getStays());
        model.addAttribute("page", stayResponse.getPage());
        model.addAttribute("roomNumber", roomNumber);
        return "room/stays";
    }

    @GetMapping("/rooms/{type}")
    public String getRoomsByType(@PathVariable("type") String type, 
                                 @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "0") int page,
                                 Model model) {
        RoomResponseDto roomResponse = roomService.getRoomsByType(type, page);
        model.addAttribute("rooms", roomResponse.getRooms());
        model.addAttribute("page", roomResponse.getPage());
        model.addAttribute("roomType", type);
        return "room/rooms";
    }

    @GetMapping("/rooms/new")
    public String showCreateRoomForm(Model model) {
        model.addAttribute("room", new CreateRoomDTO());
        return "room/create-room";
    }

    @org.springframework.web.bind.annotation.PostMapping("/rooms/new")
    public String createRoomSubmit(@org.springframework.web.bind.annotation.ModelAttribute("room") CreateRoomDTO createRoomDTO, Model model) {
        if (createRoomDTO.getUnavailable() == null) {
            createRoomDTO.setUnavailable(false);
        }
        RoomActionResponseDTO response = roomService.createRoom(createRoomDTO);
        
        model.addAttribute("popupMessage", response.getMessage());
        model.addAttribute("popupType", response.getSuccess() ? "success" : "error");
        model.addAttribute("room", createRoomDTO);
        
        return "room/create-room";
    }

    @GetMapping("/rooms/{roomNumber}/edit")
    public String showUpdateRoomForm(@PathVariable("roomNumber") int roomNumber, Model model) {
        RoomDto room = roomService.getRoomByNumber(roomNumber);
        UpdateRoomDTO updateDTO = new UpdateRoomDTO();
        updateDTO.setRoomType(com.frontend.HospitalManagement.dto.room.enums.RoomType.valueOf(room.getRoomType()));
        System.out.println(room.getBlock());
        if (room.getBlockFloor() != null && room.getBlockCode() != null) {
            updateDTO.setBlockFloor(room.getBlockFloor());
            updateDTO.setBlockCode(room.getBlockCode());
        } else if (room.getBlock() != null && room.getBlock().contains("-")) {
            String[] parts = room.getBlock().split("-");
            try {
                // Strip all non-digit characters to handle formats like "Block 1 - Space 101"
                String floorStr = parts[0].replaceAll("\\D", "");
                String codeStr = parts[1].replaceAll("\\D", "");
                
                if (!floorStr.isEmpty()) {
                    updateDTO.setBlockFloor(Integer.parseInt(floorStr));
                }
                if (!codeStr.isEmpty()) {
                    updateDTO.setBlockCode(Integer.parseInt(codeStr));
                }
            } catch (NumberFormatException ignored) {}
        }
        
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("room", updateDTO);
        return "room/update-room";
    }

    @org.springframework.web.bind.annotation.PostMapping("/rooms/{roomNumber}/edit")
    public String updateRoomSubmit(@PathVariable("roomNumber") int roomNumber, 
                                   @org.springframework.web.bind.annotation.ModelAttribute UpdateRoomDTO updateRoomDTO,
                                   Model model) {
        
        RoomActionResponseDTO response = roomService.updateRoom(updateRoomDTO, roomNumber);
        
        model.addAttribute("popupMessage", response.getMessage());
        model.addAttribute("popupType", response.getSuccess() ? "success" : "error");
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("room", updateRoomDTO);
        return "room/update-room";
    }

    @org.springframework.web.bind.annotation.PostMapping("/rooms/{roomNumber}/toggle")
    public String toggleRoomUnavailability(
            @PathVariable("roomNumber") int roomNumber,
            @org.springframework.web.bind.annotation.RequestParam("unavailable") boolean unavailable,
            @org.springframework.web.bind.annotation.RequestParam("roomType") String roomType) {
        
        UpdateRoomUnavailabilityDTO dto = new UpdateRoomUnavailabilityDTO(!unavailable);
        roomService.toggleUnavailability(dto, roomNumber);
        
        return "redirect:/rooms/" + roomType;
    }
}
