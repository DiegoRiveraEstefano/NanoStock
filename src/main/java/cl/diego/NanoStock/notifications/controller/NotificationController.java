package cl.diego.NanoStock.notifications.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.notifications.dto.NotificationResponse;
import cl.diego.NanoStock.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        if (unreadOnly) {
            return ResponseEntity.ok(notificationService.getUnreadNotifications());
        }
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.countUnread()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trigger-expiration-check")
    public ResponseEntity<Map<String, Object>> triggerExpirationCheck(
            @RequestParam(required = false, defaultValue = "7") int daysAhead) {
        int detected = notificationService.runExpirationCheck(daysAhead);
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "Auditoría de caducidad ejecutada exitosamente",
            "detectedBatchesCount", detected
        ));
    }

    @PostMapping("/trigger-replenishment-check")
    public ResponseEntity<Map<String, Object>> triggerReplenishmentCheck() {
        int replenishments = notificationService.runReplenishmentCheck();
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", "Evaluación de reposición ejecutada exitosamente",
            "suggestedItemsCount", replenishments
        ));
    }
}
