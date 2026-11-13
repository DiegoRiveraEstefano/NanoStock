package cl.diego.NanoStock.demo.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoStatusDto {

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private boolean enabled;
    private int resetIntervalDays;
    private LocalDateTime lastResetAt;
    private LocalDateTime nextResetAt;
    private long daysRemaining;
    private long hoursRemaining;
    private int lastCleanupDeletedRows;
    private String demoUsername;

    public String getFormattedNextResetAt() {
        return nextResetAt != null ? nextResetAt.format(DISPLAY_FMT) : "-";
    }

    public String getFormattedLastResetAt() {
        return lastResetAt != null ? lastResetAt.format(DISPLAY_FMT) : "-";
    }
}
