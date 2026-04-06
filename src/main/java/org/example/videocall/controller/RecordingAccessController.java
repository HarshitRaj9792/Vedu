package org.example.videocall.controller;

import lombok.RequiredArgsConstructor;
import org.example.videocall.model.Recording;
import org.example.videocall.repo.RecordingRepository;
import org.example.videocall.service.SupabaseAuthService;
import org.example.videocall.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recordings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecordingAccessController {

    private final RecordingRepository recordingRepository;
    private final TokenService tokenService;
    private final SupabaseAuthService supabaseAuthService;

    @GetMapping
    public ResponseEntity<?> getRecordings(@RequestHeader("Authorization") String authHeader) {
        String userId = tokenService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorMessage", "Invalid session"));
        }

        String role = supabaseAuthService.getUserRole(userId);

        if ("Teacher".equalsIgnoreCase(role)) {
            // Teachers only see their own recordings
            List<Recording> teacherRecordings = recordingRepository.findByTeacherIdOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(teacherRecordings);
        } else {
            // Admins and Students see all recordings
            List<Recording> allRecordings = recordingRepository.findAllByOrderByCreatedAtDesc();
            return ResponseEntity.ok(allRecordings);
        }
    }
}
