package cl.diego.NanoStock.ui;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cl.diego.NanoStock.demo.dto.DemoStatusDto;

public final class ViewContextHelper {

    public static final String ATTR_UNREAD_NOTIFICATIONS = "nanoUnreadNotifications";
    public static final String ATTR_DEMO_STATUS = "nanoDemoStatus";

    private ViewContextHelper() {
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public static String currentUsername(String fallback) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return (fallback != null && !fallback.isBlank()) ? fallback : "Familia";
    }

    public static int currentUnreadCount(int fallback) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            Object val = servletAttrs.getRequest().getAttribute(ATTR_UNREAD_NOTIFICATIONS);
            if (val instanceof Integer count) {
                return count;
            }
        }
        return fallback;
    }

    public static DemoStatusDto currentDemoStatus() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            Object val = servletAttrs.getRequest().getAttribute(ATTR_DEMO_STATUS);
            if (val instanceof DemoStatusDto status) {
                return status;
            }
        }
        return null;
    }
}
