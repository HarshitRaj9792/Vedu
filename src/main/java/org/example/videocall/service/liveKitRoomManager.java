package org.example.videocall.service;

import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class liveKitRoomManager {

    // Logic: This class ONLY uses the client. It does NOT create it.
    private final RoomServiceClient roomClient;

    public List<LivekitModels.Room> listAllRooms() {
        try {
            var response = roomClient.listRooms(List.of()).execute().body();
            return response != null ? response : List.of();
        } catch (Exception e) {
            log.error("Failed to list all rooms: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean isRoomActive(String roomName) {
        try {
            var response = roomClient.listRooms(List.of(roomName)).execute().body();
            boolean active = response != null && !response.isEmpty();
            log.info("Checking room status for {}: {}", roomName, active ? "ACTIVE" : "INACTIVE");
            return active;
        } catch (Exception e) {
            log.error("Failed to check if room {} is active: {}", roomName, e.getMessage());
            return false;
        }
    }

    public List<?> getConnectedClients(String roomName) {
        try {
            var response = roomClient.listParticipants(roomName).execute().body();
            return response != null ? response : List.of();
        } catch (Exception e) {
            log.error("Error fetching participant list for {}: {}", roomName, e.getMessage());
            return List.of();
        }
    }

    public void closeRoom(String roomName) {
        try {
            roomClient.deleteRoom(roomName).execute();
            log.info("Teacher closed room: {}", roomName);
        } catch (Exception e) {
            log.error("Could not close room {}: {}", roomName, e.getMessage());
        }
    }
}