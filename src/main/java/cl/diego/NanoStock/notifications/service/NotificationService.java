package cl.diego.NanoStock.notifications.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.inventory.dto.StockBatchResponse;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.notifications.dto.NotificationResponse;
import cl.diego.NanoStock.notifications.event.StockExpiringSoonEvent;
import cl.diego.NanoStock.notifications.event.StockReplenishmentNeededEvent;
import cl.diego.NanoStock.notifications.model.Notification;
import cl.diego.NanoStock.notifications.model.NotificationSeverity;
import cl.diego.NanoStock.notifications.model.NotificationType;
import cl.diego.NanoStock.notifications.repository.NotificationRepository;
import cl.diego.NanoStock.purchases.dto.ShoppingListResponse;
import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final StockBatchRepository stockBatchRepository;
    private final ShoppingListService shoppingListService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Transactional
    public NotificationResponse createNotification(
            String title,
            String message,
            NotificationType type,
            NotificationSeverity severity,
            Long referenceId) {

        Notification notification = Notification.builder()
            .title(title)
            .message(message)
            .notificationType(type)
            .severity(severity != null ? severity : NotificationSeverity.INFO)
            .referenceId(referenceId)
            .isRead(false)
            .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notificación generada [#{}]: {} [{}]", saved.getId(), saved.getTitle(), saved.getSeverity());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByIsReadFalse();
    }

    @Transactional
    public Optional<NotificationResponse> markAsRead(Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setRead(true);
            return toResponse(notificationRepository.save(notif));
        });
    }

    @Transactional
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public int runExpirationCheck(int daysAhead) {
        int targetDays = daysAhead > 0 ? daysAhead : 7;
        log.info("Iniciando auditoría de caducidad de lotes para los próximos {} días...", targetDays);

        List<StockBatchResponse> expiringBatches = inventoryService.getExpiringBatches(targetDays);
        LocalDate today = LocalDate.now();

        for (StockBatchResponse batch : expiringBatches) {
            long daysRemaining = batch.getExpirationDate() != null
                ? ChronoUnit.DAYS.between(today, batch.getExpirationDate())
                : 0L;

            NotificationSeverity severity = daysRemaining <= 2
                ? NotificationSeverity.CRITICAL
                : NotificationSeverity.WARNING;

            String title = String.format("¡Atención! '%s' vence en %d días", batch.getProductName(), daysRemaining);
            String message = String.format("El lote #%d con %.2f unidades ubicado en '%s' caduca el %s. Consúmelo antes para evitar desperdicio.",
                batch.getId(), batch.getQuantity(), batch.getLocationName(), batch.getExpirationDate());

            createNotification(title, message, NotificationType.EXPIRATION_ALERT, severity, batch.getId());

            eventPublisher.publishEvent(new StockExpiringSoonEvent(
                batch.getProductId(),
                batch.getProductName(),
                batch.getId(),
                batch.getLocationName(),
                batch.getQuantity(),
                batch.getExpirationDate(),
                daysRemaining
            ));
        }

        log.info("Auditoría de caducidad finalizada: {} lotes próximos a vencer detectados", expiringBatches.size());
        return expiringBatches.size();
    }

    @Transactional
    public int runReplenishmentCheck() {
        log.info("Iniciando evaluación de quiebres de stock y reposición automática...");

        ShoppingList activeList = shoppingListService.getOrCreateActiveList();
        ShoppingListResponse listResponse = shoppingListService.generateSuggestedItems(activeList.getId());

        int suggestedCount = listResponse.getItems() != null ? listResponse.getItems().size() : 0;

        if (suggestedCount > 0) {
            String title = "Reposición sugerida de despensa";
            String message = String.format("Se han identificado %d productos bajo su umbral mínimo de stock y se han añadido a la lista de compras activa.", suggestedCount);

            createNotification(title, message, NotificationType.STOCK_LOW_ALERT, NotificationSeverity.WARNING, activeList.getId());

            // Emitir evento por cada producto en quiebre
            List<Product> products = productRepository.findAll();
            for (Product p : products) {
                Double stock = stockBatchRepository.sumQuantityByProductId(p.getId());
                if (stock <= p.getMinStockThreshold()) {
                    eventPublisher.publishEvent(new StockReplenishmentNeededEvent(
                        p.getId(),
                        p.getName(),
                        stock,
                        p.getMinStockThreshold(),
                        p.getTargetStockQuantity()
                    ));
                }
            }
        }

        log.info("Evaluación de reposición completada: {} ítems sugeridos para reabastecimiento", suggestedCount);
        return suggestedCount;
    }

    public boolean sendEmailAlert(String to, String subject, String body) {
        if (mailSender == null) {
            log.info("JavaMailSender no disponible o no configurado en este entorno. Correo simulado a '{}': {}", to, subject);
            return false;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Correo de alerta despachado exitosamente a {}", to);
            return true;
        } catch (Exception ex) {
            log.warn("No fue posible despachar correo de alerta a {}: {}", to, ex.getMessage());
            return false;
        }
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .title(n.getTitle())
            .message(n.getMessage())
            .notificationType(n.getNotificationType())
            .severity(n.getSeverity())
            .isRead(n.isRead())
            .referenceId(n.getReferenceId())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
