package org.example.videocall.controller;

import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.service.liveKitRoomManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final liveKitRoomManager roomManager;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM, HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Main dashboard data: all rooms + participants + summary stats.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        try {
            List<LivekitModels.Room> rooms = roomManager.listAllRooms();

            int totalParticipants = rooms.stream()
                    .mapToInt(LivekitModels.Room::getNumParticipants)
                    .sum();

            List<Map<String, Object>> roomData = rooms.stream().map(room -> {
                // Fetch participants for this room
                List<?> participants = roomManager.getConnectedClients(room.getName());
                List<String> names = participants.stream()
                        .filter(p -> p instanceof LivekitModels.ParticipantInfo)
                        .map(p -> ((LivekitModels.ParticipantInfo) p).getIdentity())
                        .collect(Collectors.toList());

                return Map.<String, Object>of(
                        "name",             room.getName(),
                        "participants",     room.getNumParticipants(),
                        "participantNames", names,
                        "activeRecording",  room.getActiveRecording(),
                        "createdAt",        FMT.format(
                                Instant.ofEpochSecond(room.getCreationTime()))
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status",            "ok",
                    "totalRooms",        rooms.size(),
                    "totalParticipants", totalParticipants,
                    "rooms",             roomData
            ));

        } catch (Exception e) {
            log.error("Admin dashboard error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status",            "error",
                    "error",             e.getMessage(),
                    "totalRooms",        0,
                    "totalParticipants", 0,
                    "rooms",             List.of()
            ));
        }
    }

    /**
     * Force-close a room (admin action).
     */
    @DeleteMapping("/rooms/{roomName}")
    public ResponseEntity<Map<String, String>> closeRoom(@PathVariable String roomName) {
        try {
            roomManager.closeRoom(roomName);
            return ResponseEntity.ok(Map.of("message", "Room " + roomName + " closed."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
