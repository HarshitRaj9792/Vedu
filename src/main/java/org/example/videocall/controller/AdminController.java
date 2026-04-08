package org.example.videocall.controller;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import livekit.LivekitModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.model.Recording;
import org.example.videocall.model.Schedules;
import org.example.videocall.repo.RecordingRepository;
import org.example.videocall.repo.Schedules_repo;
import org.example.videocall.service.liveKitRoomManager;
import org.springframework.beans.factory.annotation.Value;
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
    private final RecordingRepository recordingRepository;
    private final Schedules_repo schedulesRepo;

    @Value("${MINIO_ENDPOINT:http://minio:9000}")
    private String minioEndpoint;

    @Value("${MINIO_ACCESS_KEY:minioadmin}")
    private String minioAccessKey;

    @Value("${MINIO_SECRET_KEY:minioadmin}")
    private String minioSecretKey;

    @Value("${MINIO_BUCKET:recordings}")
    private String minioBucket;

    @Value("${MINIO_PUBLIC_URL:http://localhost:9000}")
    private String minioPublicUrl;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault());

    // ── Dashboard ──────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        try {
            List<LivekitModels.Room> rooms = roomManager.listAllRooms();

            int totalParticipants = rooms.stream()
                    .mapToInt(LivekitModels.Room::getNumParticipants)
                    .sum();

            List<Map<String, Object>> roomData = rooms.stream().map(room -> {
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
                        "createdAt",        FMT.format(Instant.ofEpochSecond(room.getCreationTime()))
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

    // ── Force-close a room ─────────────────────────────────────────────────────

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

    // ── List all recordings ────────────────────────────────────────────────────

    @GetMapping("/recordings")
    public ResponseEntity<List<Recording>> listRecordings() {
        // Only completed recordings (fileUrl set) — pending rows (fileUrl='') excluded
        return ResponseEntity.ok(recordingRepository.findAllCompletedOrderByCreatedAtDesc());
    }

    // ── Delete a recording (DB + MinIO file) ───────────────────────────────────

    @DeleteMapping("/recordings/{id}")
    public ResponseEntity<Map<String, String>> deleteRecording(@PathVariable Long id) {
        return recordingRepository.findById(id).map(recording -> {
            String objectKey = extractObjectKey(recording.getFileUrl());


            try {
                minioClient().removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(minioBucket)
                                .object(objectKey)
                                .build()
                );
                log.info("Deleted MinIO object: bucket={}, key={}", minioBucket, objectKey);
            } catch (Exception e) {
                log.warn("Could not delete MinIO file for recording {} (key={}): {}", id, objectKey, e.getMessage());
            }

            // 2. Always remove the DB record
            recordingRepository.deleteById(id);
            log.info("Admin deleted recording id={} room={}", id, recording.getRoomName());

            return ResponseEntity.ok(Map.of("message", "Recording deleted successfully."));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Singleton MinioClient — reused across requests (thread-safe). */
    private volatile MinioClient _minioClient;
    private MinioClient minioClient() {
        if (_minioClient == null) {
            synchronized (this) {
                if (_minioClient == null) {
                    _minioClient = MinioClient.builder()
                            .endpoint(minioEndpoint)          // e.g. http://minio:9000  (Docker-internal)
                            .credentials(minioAccessKey, minioSecretKey)
                            .build();
                    log.info("MinioClient initialised → endpoint={}", minioEndpoint);
                }
            }
        }
        return _minioClient;
    }

    // ── Extract MinIO object key from public URL ───────────────────────────────

    /**
     * Converts the public fileUrl back to the MinIO object key.
     * from "https://vedulive.net2coder.in/storage/java01-1234567890.mp4"
     *   to  "java01-1234567890.mp4"
     */
    private String extractObjectKey(String fileUrl) {
        try {
            String base = minioPublicUrl.endsWith("/")
                    ? minioPublicUrl.substring(0, minioPublicUrl.length() - 1)
                    : minioPublicUrl;
            String key = fileUrl.startsWith(base) ? fileUrl.substring(base.length()) : fileUrl;
            if (key.startsWith("/")) key = key.substring(1);
            // Strip bucket prefix if present (e.g. "recordings/file.mp4" → "file.mp4")
            if (key.startsWith(minioBucket + "/")) key = key.substring(minioBucket.length() + 1);
            return key;
        } catch (Exception e) {
            return fileUrl;
        }
    }

    // ── Schedule Management (Admin only) ───────────────────────────────────────

    /** GET all schedules — admin sees everything */
    @GetMapping("/schedules")
    public ResponseEntity<?> getAllSchedules() {
        return ResponseEntity.ok(schedulesRepo.findAll());
    }

    /** DELETE any schedule — no status restriction for admin */
    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        return schedulesRepo.findById(id).map(s -> {
            schedulesRepo.delete(s);
            return ResponseEntity.ok(java.util.Map.of("deleted", id));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** PATCH status — admin can force any status */
    @PatchMapping("/schedules/{id}/status")
    public ResponseEntity<?> setScheduleStatus(@PathVariable Long id, @RequestParam String status) {
        java.util.Set<String> valid = java.util.Set.of("UPCOMING", "LIVE", "COMPLETED");
        if (!valid.contains(status)) return ResponseEntity.badRequest().body("Invalid status");
        return schedulesRepo.findById(id).map(s -> {
            s.setStatus(status);
            return ResponseEntity.ok(schedulesRepo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }
}
