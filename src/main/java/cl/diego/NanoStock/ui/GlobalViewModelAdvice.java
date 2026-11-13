package cl.diego.NanoStock.ui;

import java.security.Principal;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import cl.diego.NanoStock.demo.config.DemoProperties;
import cl.diego.NanoStock.demo.dto.DemoStatusDto;
import cl.diego.NanoStock.demo.service.DemoResetService;
import cl.diego.NanoStock.notifications.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@ControllerAdvice(basePackages = "cl.diego.NanoStock.ui")
@RequiredArgsConstructor
public class GlobalViewModelAdvice {

    private final NotificationService notificationService;
    private final DemoProperties demoProperties;
    private final DemoResetService demoResetService;

    @ModelAttribute
    public void populateCommonAttributes(Principal principal, HttpServletRequest request, Model model) {
        String username = (principal != null) ? principal.getName() : "Familia";
        int unreadCount = 0;

        if (principal != null) {
            try {
                unreadCount = (int) notificationService.countUnread();
            } catch (Exception ignored) {
                unreadCount = 0;
            }
        }

        request.setAttribute(ViewContextHelper.ATTR_UNREAD_NOTIFICATIONS, unreadCount);
        if (!model.containsAttribute("username")) {
            model.addAttribute("username", username);
        }
        if (!model.containsAttribute("unreadNotifications")) {
            model.addAttribute("unreadNotifications", unreadCount);
        }

        if (demoProperties.isEnabled()) {
            try {
                DemoStatusDto status = demoResetService.getStatus();
                request.setAttribute(ViewContextHelper.ATTR_DEMO_STATUS, status);
                if (!model.containsAttribute("demoStatus")) {
                    model.addAttribute("demoStatus", status);
                }
            } catch (Exception ignored) {
                // Evita afectar el renderizado de vistas si el esquema aún no está listo
            }
        }
    }
}
