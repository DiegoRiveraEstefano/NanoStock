package cl.diego.NanoStock.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.notifications.service.NotificationService;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final ProductCatalogService catalogService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final ShoppingListService shoppingListService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "Familia";

        int totalProducts = catalogService.getAllProducts().size();
        int expiringSoonCount = inventoryService.getExpiringBatches(7).size();
        int criticalStockCount = inventoryService.getLowStockProducts().size();
        int unreadNotifications = (int) notificationService.countUnread();

        int shoppingListCount = 0;
        try {
            var activeList = shoppingListService.getOrCreateActiveList();
            shoppingListCount = activeList.getItems() != null ? activeList.getItems().size() : 0;
        } catch (Exception ignored) {
            // Manejo tolerante si la base está recién inicializada
        }

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("criticalStockCount", criticalStockCount);
        model.addAttribute("expiringSoonCount", expiringSoonCount);
        model.addAttribute("shoppingListCount", shoppingListCount);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("username", username);

        return "pages/dashboard/index";
    }
}
