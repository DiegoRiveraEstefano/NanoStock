package cl.diego.NanoStock.notifications.dto;

import java.time.LocalDateTime;

import cl.diego.NanoStock.notifications.model.NotificationSeverity;
import cl.diego.NanoStock.notifications.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private NotificationSeverity severity;
    private boolean isRead;
    private Long referenceId;
    private LocalDateTime createdAt;
}
