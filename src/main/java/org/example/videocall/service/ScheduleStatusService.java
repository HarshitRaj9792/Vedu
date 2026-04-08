package org.example.videocall.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.repo.Schedules_repo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Automatically marks sessions as COMPLETED once 3 hours have passed
 * since the scheduled topicTime. Runs every 30 minutes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleStatusService {

    private final Schedules_repo schedulesRepo;

    @Scheduled(fixedDelay = 30 * 60 * 1000)   // every 30 minutes
    @Transactional
    public void autoComplete() {
        // Sessions whose topicTime is more than 3 hours ago → COMPLETED
        LocalDateTime cutoff = LocalDateTime.now().minusHours(3);
        int updated = schedulesRepo.autoCompleteOld(cutoff);
        if (updated > 0) {
            log.info("ScheduleStatusService: auto-completed {} stale session(s)", updated);
        }
    }
}
