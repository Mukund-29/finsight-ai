package com.finsight.service;

import com.finsight.entity.Request;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Timer Service for tracking request queue times
 * 
 * @author Mukund Kute
 */
@Service
public class TimerService {

    /**
     * Calculate time spent in OPEN queue (from creation to assignment)
     */
    public Duration getTimeInOpenQueue(Request request) {
        if (request.getAssignedAt() == null) {
            // Still in open queue
            if (request.getCreatedAt() != null) {
                return Duration.between(request.getCreatedAt(), LocalDateTime.now());
            }
            return Duration.ZERO;
        }
        // Was in open queue until assigned
        if (request.getCreatedAt() != null) {
            return Duration.between(request.getCreatedAt(), request.getAssignedAt());
        }
        return Duration.ZERO;
    }

    /**
     * Calculate time spent in DEVELOPER queue (from assignment to now)
     */
    public Duration getTimeInDeveloperQueue(Request request) {
        if (request.getAssignedAt() == null) {
            return Duration.ZERO;
        }
        return Duration.between(request.getAssignedAt(), LocalDateTime.now());
    }

    /**
     * Calculate time until ETA
     */
    public Duration getTimeUntilEta(Request request) {
        if (request.getEta() == null) {
            return null;
        }
        return Duration.between(LocalDateTime.now(), request.getEta());
    }

    /**
     * Check if ETA is approaching (within threshold minutes)
     */
    public boolean isEtaApproaching(Request request, int thresholdMinutes) {
        if (request.getEta() == null) {
            return false;
        }
        Duration timeUntilEta = getTimeUntilEta(request);
        if (timeUntilEta == null) {
            return false;
        }
        long minutesUntilEta = timeUntilEta.toMinutes();
        return minutesUntilEta > 0 && minutesUntilEta <= thresholdMinutes;
    }

    /**
     * Check if ETA has passed
     */
    public boolean isEtaExceeded(Request request) {
        if (request.getEta() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(request.getEta());
    }

    /**
     * Format duration to human-readable string
     */
    public String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0 minutes";
        }
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }
        if (hours > 0) {
            sb.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }
        if (minutes > 0 || sb.length() == 0) {
            sb.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
        }
        return sb.toString().trim();
    }
}
