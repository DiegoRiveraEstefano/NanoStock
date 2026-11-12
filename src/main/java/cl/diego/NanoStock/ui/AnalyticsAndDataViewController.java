package cl.diego.NanoStock.ui;

import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.diego.NanoStock.analytics.dto.ConsumptionAutonomyResponse;
import cl.diego.NanoStock.analytics.dto.WasteReportResponse;
import cl.diego.NanoStock.analytics.service.ConsumptionAnalyticsService;
import cl.diego.NanoStock.analytics.service.WasteAnalyticsService;
import cl.diego.NanoStock.dataimport.dto.ImportConflictStrategy;
import cl.diego.NanoStock.dataimport.dto.ImportResultDto;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.service.InventoryImportService;
import cl.diego.NanoStock.dataimport.service.ProductImportService;
import cl.diego.NanoStock.notifications.dto.NotificationResponse;
import cl.diego.NanoStock.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AnalyticsAndDataViewController {

    private final ConsumptionAnalyticsService consumptionAnalyticsService;
    private final WasteAnalyticsService wasteAnalyticsService;
    private final NotificationService notificationService;
    private final ProductImportService productImportService;
    private final InventoryImportService inventoryImportService;

    // --- Analíticas ---
    @GetMapping("/analytics")
    public String analyticsDashboard(Model model) {
        List<ConsumptionAutonomyResponse> autonomies = consumptionAnalyticsService.getAllAutonomy();
        LocalDate now = LocalDate.now();
        WasteReportResponse wasteReport = wasteAnalyticsService.generateWasteReport(now.minusMonths(1), now);

        model.addAttribute("autonomies", autonomies);
        model.addAttribute("wasteReport", wasteReport);
        return "pages/analytics/index";
    }

    // --- Notificaciones ---
    @GetMapping("/notifications")
    public String notifications(
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        model.addAttribute("notifications", notifications);
        model.addAttribute("flashMessage", flashMessage);
        model.addAttribute("flashType", flashType);
        return "pages/notifications/index";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead();
        redirectAttributes.addFlashAttribute("flashMessage", "Todas las notificaciones han sido marcadas como leídas.");
        return "redirect:/notifications";
    }

    // --- Importación & Exportación de Datos ---
    @GetMapping("/data/import")
    public String importPage(Model model) {
        return "pages/data/import";
    }

    @PostMapping("/data/import")
    public String handleDataImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("targetType") String targetType,
            @RequestParam(value = "conflictStrategy", defaultValue = "UPDATE") ImportConflictStrategy conflictStrategy,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
            Principal principal,
            Model model) {

        try {
            DataFormat format = DataFormat.fromFilename(file.getOriginalFilename());
            ImportResultDto result;

            try (InputStream is = file.getInputStream()) {
                if ("INVENTORY".equalsIgnoreCase(targetType)) {
                    result = inventoryImportService.importInventory(is, format, dryRun, true);
                } else {
                    result = productImportService.importProducts(is, format, dryRun, conflictStrategy, true);
                }
            }

            model.addAttribute("importResult", result);
            model.addAttribute("flashMessage", "Archivo procesado: " + file.getOriginalFilename());
        } catch (Exception ex) {
            model.addAttribute("flashMessage", "Error al procesar archivo: " + ex.getMessage());
            model.addAttribute("flashType", "is-danger");
        }

        return "pages/data/import";
    }

    @GetMapping("/data/export")
    public String exportPage() {
        return "pages/data/export";
    }
}
