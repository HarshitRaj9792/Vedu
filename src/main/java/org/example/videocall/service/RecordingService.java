package org.example.videocall.service;

import io.livekit.server.EgressServiceClient;
import livekit.LivekitEgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.model.Recording;
import org.example.videocall.repo.RecordingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecordingService {

    @Value("${MINIO_ACCESS_KEY:minioadmin}")
    private String miniOKey;

    @Value("${MINIO_SECRET_KEY:minioadmin}")
    private String miniOSecret;

    @Value("${MINIO_BUCKET:recordings}")
    private String miniOBucket;

    // This is the endpoint the Egress CONTAINER uses to reach MinIO (always Docker-internal)
    @Value("${MINIO_ENDPOINT:http://minio:9000}")
    private String miniOEndpoint;

    private final EgressServiceClient egressServiceClient;
    private final RecordingRepository recordingRepository;

    public String startRecording(String roomName, String teacherId) throws Exception {

        Thread.sleep(2000);

        LivekitEgress.S3Upload s3 = LivekitEgress.S3Upload.newBuilder()
                .setAccessKey(this.miniOKey)
                .setSecret(this.miniOSecret)
                .setBucket(this.miniOBucket)
                .setEndpoint(this.miniOEndpoint)
                .setRegion("us-east-1")
                .setForcePathStyle(true)
                .build();

        String fileName = roomName + "-" + System.currentTimeMillis() + ".mp4";

        LivekitEgress.EncodedFileOutput fileOutput = LivekitEgress.EncodedFileOutput.newBuilder()
                .setFilepath(fileName)
                .setS3(s3)
                .build();

        var response = egressServiceClient.startRoomCompositeEgress(
                roomName,
                fileOutput,
                "speaker"  // layout
        ).execute();

        if (response.body() == null) {
            throw new RuntimeException("Failed to start recording: empty response");
        }

        String egressId = response.body().getEgressId();
        log.info("Recording started for room {}: egressId={}", roomName, egressId);

        // ── Persist a pending row immediately so teacherId survives a backend restart ──
        // fileUrl is set to "" now; the egress_ended webhook will update it with the real URL.
        Recording pending = new Recording();
        pending.setEgressId(egressId);
        pending.setTeacherId(teacherId);
        pending.setRoomName(roomName);
        pending.setFileUrl("");   // placeholder — updated by webhook
        recordingRepository.save(pending);
        log.info("Saved pending recording row for egressId={} teacher={}", egressId, teacherId);

        return egressId;
    }

    public void stopRecording(String roomName) throws Exception {
        // Find the most recent active (fileUrl empty) recording for this room
        Recording active = recordingRepository
                .findTopByRoomNameAndFileUrlOrderByCreatedAtDesc(roomName, "");
        if (active == null) {
            throw new IllegalStateException("No active recording found for room: " + roomName);
        }
        egressServiceClient.stopEgress(active.getEgressId()).execute();
        log.info("Recording stopped for room {}: egressId={}", roomName, active.getEgressId());
    }

    public boolean isRecording(String roomName) {
        return recordingRepository
                .findTopByRoomNameAndFileUrlOrderByCreatedAtDesc(roomName, "") != null;
    }
}