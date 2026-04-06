package org.example.videocall.service;

import io.livekit.server.EgressServiceClient;
import livekit.LivekitEgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    // Track the active egressId per room for stopping
    private final Map<String, String> activeEgress = new ConcurrentHashMap<>();

    public String startRecording(String roomName) throws Exception {
        // S3/MinIO output config

        Thread.sleep(2000);
        LivekitEgress.S3Upload s3 = LivekitEgress.S3Upload.newBuilder()
                .setAccessKey("minioadmin")  /// hardcoded the envs
                .setSecret("minioadmin")
                .setBucket("recordings")
                .setEndpoint("http://minio:9000")
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
        activeEgress.put(roomName, egressId);
        log.info("Recording started for room {}: egressId={}", roomName, egressId);
        return egressId;
    }

    public void stopRecording(String roomName) throws Exception {
        String egressId = activeEgress.get(roomName);


        if (egressId == null) {
            throw new IllegalStateException("No active recording for room: " + roomName);
        }

        activeEgress.remove(roomName);
        egressServiceClient.stopEgress(egressId).execute();
        log.info("Recording stopped for room {}: egressId {}", roomName, egressId);
    }

    public boolean isRecording(String roomName) {
        return activeEgress.containsKey(roomName);
    }
}