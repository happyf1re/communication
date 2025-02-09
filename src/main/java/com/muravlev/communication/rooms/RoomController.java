package com.muravlev.communication.rooms;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // GET /api/rooms  -> вернёт список всех комнат
    @GetMapping
    public List<String> getRooms() {
        return roomService.getAllRooms();
    }

    // POST /api/rooms?name=CallRoom_123  -> создать новую
    @PostMapping
    public void createRoom(@RequestParam("name") String roomName) {
        roomService.createRoom(roomName);
    }

    // DELETE /api/rooms?name=CallRoom_123 -> удалить
    @DeleteMapping
    public void deleteRoom(@RequestParam("name") String roomName) {
        roomService.removeRoom(roomName);
    }
}

