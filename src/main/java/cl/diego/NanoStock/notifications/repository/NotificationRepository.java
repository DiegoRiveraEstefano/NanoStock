package cl.diego.NanoStock.notifications.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.notifications.model.Notification;
import cl.diego.NanoStock.notifications.model.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByNotificationTypeOrderByCreatedAtDesc(NotificationType notificationType);

    long countByIsReadFalse();
}
