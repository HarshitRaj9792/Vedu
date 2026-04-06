package org.example.videocall.controller;

import livekit.LivekitModels;
import org.example.videocall.service.RecordingService;
import org.example.videocall.service.liveKitRoomManager;
import org.example.videocall.service.TokenService;
import io.livekit.server.*;
import livekit.LivekitWebhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.service.SupabaseAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.videocall.repo.RecordingRepository;
import org.example.videocall.model.Recording;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    @Value("${LIVEKIT_API_KEY}")
    private String livekitApiKey;

    @Value("${OPENVIDU_SECRET}")
    private String livekitApiSecret;

    @Value("${MINIO_PUBLIC_URL:http://localhost:9000}")
    private String minioPublicUrl;
    @Autowired
    private  TokenService tokenService;
    @Autowired
    private  SupabaseAuthService supabaseAuthService;
    @Autowired
    private  liveKitRoomManager livekitRoomManager;
    @Autowired
    private RecordingService recordingService;
    @Autowired
    private RecordingRepository recordingRepository;

    /**
     * Updated endpoint to match the discovered logic.
     * Accessible by both Teachers and Students.
     */
    @PostMapping(value = "/token")
    public ResponseEntity<Map<String, String>> createToken(@RequestHeader("Authorization") String authHeader,@RequestBody Map<String, String> params) {
        String roomName = params.get("roomName");
        String participantName = params.get("participantName");
        String userId = tokenService.extractUserIdFromToken(authHeader);

        // Basic validation
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorMessage", "Invalid session"));
        }
        if (roomName == null || participantName == null || roomName.isEmpty() || participantName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errorMessage", "roomName and participantName are required"));
        }

        try {
            String userRole = supabaseAuthService.getUserRole(userId);
            boolean roomExists = livekitRoomManager.isRoomActive(roomName);
            if ("Student".equalsIgnoreCase(userRole) && !roomExists) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("errorMessage", "Classroom not started. Please wait for the teacher."));
            }
            // 1. Initialize the AccessToken with your keys
            AccessToken token = new AccessToken(livekitApiKey, livekitApiSecret);

            // 2. Set the User's Identity and Name
            token.setName(participantName);
            token.setIdentity(participantName);

            // 3. Apply the grants using the LiveKit library structure
            // This combines your found code with the necessary SDK objects
            token.addGrants(new RoomJoin(true));
            token.addGrants(new RoomName(roomName));

            // Optional: You can still allow room creation automatically
            token.addGrants(new RoomCreate(true));

            log.info("Token generated for participant: {} in room: {}", participantName, roomName);

            // 4. Return the token and roomName to the frontend
            return ResponseEntity.ok(Map.of(
                    "token", token.toJwt(),
                    "roomName", roomName
            ));

        } catch (Exception e) {
            log.error("Token generation error: ", e);
            return ResponseEntity.internalServerError().body(Map.of("errorMessage", "Failed to generate token"));
        }
    }

    @PostMapping("/end_room")
    public ResponseEntity<Void> endRoom(@RequestBody Map<String,String> payload){
        String roomName = payload.get("roomName");
        try {
            livekitRoomManager.closeRoom(roomName);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/livekit/webhook", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(@RequestHeader("Authorization") String authHeader, @RequestBody String body) {
        // The WebhookReceiver validates that the message actually came from YOUR LiveKit server
        WebhookReceiver webhookReceiver = new WebhookReceiver(livekitApiKey, livekitApiSecret);

        try {
            LivekitWebhook.WebhookEvent event = webhookReceiver.receive(body, authHeader);

            // Example: Logging who joined
            if (event.getEvent().equals("participant_joined")) {
                log.info("Student {} has entered the classroom!", event.getParticipant().getIdentity());
            }

            // Example: Detect when class ends to update your database
            if (event.getEvent().equals("room_finished")) {
                log.info("Classroom {} has closed.", event.getRoom().getName());
            }

            // Detect when recording (egress) finishes to save to DB
            if (event.getEvent().equals("egress_ended")) {
                livekit.LivekitEgress.EgressInfo egressInfo = event.getEgressInfo();
                if (egressInfo != null) {
                    String egressId = egressInfo.getEgressId();
                    String teacherId = recordingService.getTeacherIdForEgress(egressId);
                    String room = egressInfo.getRoomName();
                    
                    String fileUrl = "";
                    if (egressInfo.getFileResultsCount() > 0) {
                        fileUrl = egressInfo.getFileResults(0).getLocation();
                    } else if (egressInfo.hasFile()) {
                        fileUrl = egressInfo.getFile().getLocation();
                    }

                    if (teacherId != null && !fileUrl.isEmpty()) {
                        Recording recording = new Recording();
                        recording.setTeacherId(teacherId);
                        recording.setRoomName(room != null ? room : "Unknown Room");
                        // Egress returns internal docker network url like "http://minio:9000/recordings/..."
                        // We extract the path and bind it to the dynamic public URL (Azure IP + port or Localhost)
                        String pathSegment = fileUrl;
                        try {
                            if (fileUrl.startsWith("http")) {
                                pathSegment = new java.net.URL(fileUrl).getPath();
                            }
                        } catch (Exception ignored) { }
                        
                        if (pathSegment.startsWith("/")) {
                            pathSegment = pathSegment.substring(1);
                        }

                        String finalUrl = minioPublicUrl;
                        if (!finalUrl.endsWith("/")) finalUrl += "/";
                        finalUrl += pathSegment;

                        recording.setFileUrl(finalUrl);
                        recordingRepository.save(recording);
                        
                        recordingService.removeTeacherIdMapping(egressId);
                        log.info("Saved recording artifact for teacher {} -> {}", teacherId, fileUrl);
                    } else {
                        log.warn("Egress {} completed but missing teacher mapping or URL", egressId);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Webhook validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
        }
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/test/{roomName}")
    public List<String> testRoom(@PathVariable String roomName) {
    var participants = livekitRoomManager.getConnectedClients(roomName);

    return participants.stream()
            .filter(p -> p instanceof LivekitModels.ParticipantInfo)
            .map(p ->((LivekitModels.ParticipantInfo) p).getIdentity()).toList();
    }


/// room recording APIs /////
    @PostMapping("recording/start")
    public ResponseEntity<?> startRecording(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> param) {

        String userId = tokenService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorMessage", "Invalid session"));
        }
        String role = supabaseAuthService.getUserRole(userId);
        if ("Student".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("errorMessage", "only teacher can record"));
        }

        String roomName = param.get("roomName");
        try{
            String egressId = recordingService.startRecording(roomName, userId);
            return  ResponseEntity.ok(Map.of("egressId", egressId,"status", "recording"));
        }catch (Exception e){
            log.error("Recording failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("errorMessage", e.getMessage()));
        }
    }

    @PostMapping("recording/stop")
    public ResponseEntity<?> stopRecording(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> param) {

    String userId = tokenService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorMessage", "Invalid session"));
    }

    String role = supabaseAuthService.getUserRole(userId);
        if ("Student".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body(Map.of("errorMessage", "only teacher can record"));
        }

    String roomName = param.get("roomName");
        try {
           recordingService.stopRecording(roomName);
            return ResponseEntity.ok(Map.of("status", "stopped"));

        }catch (Exception e){
            return  ResponseEntity.internalServerError().body(Map.of("errorMessage", e.getMessage()));
        }
    }



    @GetMapping("/recording/status")
    public ResponseEntity<?> recordingStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String room) {
        String userId = tokenService.extractUserIdFromToken(authHeader);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        boolean active = recordingService.isRecording(room);
        return ResponseEntity.ok(Map.of("recording", active));
    }

}