package com.muravlev.communication.rooms;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RoomService {

    // Простейший список активных комнат
    private final List<String> rooms = new CopyOnWriteArrayList<>();

    public List<String> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public void createRoom(String roomName) {
        if (!rooms.contains(roomName)) {
            rooms.add(roomName);
        }
    }

    public void removeRoom(String roomName) {
        rooms.remove(roomName);
    }
}

