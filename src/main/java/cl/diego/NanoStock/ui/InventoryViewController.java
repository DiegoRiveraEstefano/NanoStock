package cl.diego.NanoStock.ui;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.ConsumeStockRequest;
import cl.diego.NanoStock.inventory.dto.InventoryMovementResponse;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.dto.ProductStockOverview;
import cl.diego.NanoStock.inventory.dto.RegisterWasteRequest;
import cl.diego.NanoStock.inventory.dto.StockBatchResponse;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryViewController {

    private final InventoryService inventoryService;
    private final ProductCatalogService catalogService;

    @GetMapping
    public String inventoryOverview(
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {

        List<ProductStockOverview> overviews = inventoryService.getAllStockOverview();
        List<StockBatchResponse> expiringBatches = inventoryService.getExpiringBatches(7);

        model.addAttribute("stockOverviews", overviews);
        model.addAttribute("locations", inventoryService.getAllLocations());
        model.addAttribute("allProducts", catalogService.getAllProducts());
        model.addAttribute("expiringBatches", expiringBatches);
        if (flashMessage != null) {
            model.addAttribute("flashMessage", flashMessage);
            model.addAttribute("flashType", flashType);
        } else if (!model.containsAttribute("flashMessage")) {
            model.addAttribute("flashMessage", null);
            model.addAttribute("flashType", flashType);
        }

        return "pages/inventory/index";
    }

    @GetMapping("/locations")
    public String listLocations(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {

        List<LocationResponse> locations = inventoryService.getAllLocations();
        model.addAttribute("locations", locations);
        model.addAttribute("storageTypes", StorageType.values());

        if (locationId != null) {
            inventoryService.getLocationById(locationId).ifPresent(selected -> {
                model.addAttribute("selectedLocation", selected);
                model.addAttribute("locationBatches", inventoryService.getActiveBatchesByLocation(locationId));
            });
        }

        if (flashMessage != null) {
            model.addAttribute("flashMessage", flashMessage);
            model.addAttribute("flashType", flashType);
        } else if (!model.containsAttribute("flashMessage")) {
            model.addAttribute("flashMessage", null);
            model.addAttribute("flashType", flashType);
        }

        return "pages/inventory/locations";
    }

    @PostMapping("/locations")
    public String createLocation(
            @ModelAttribute LocationRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            LocationResponse created = inventoryService.createLocation(request);
            redirectAttributes.addFlashAttribute("flashMessage",
                "Ubicación '" + created.getName() + "' registrada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al registrar ubicación: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/inventory/locations";
    }

    @PostMapping("/locations/{id}/update")
    public String updateLocation(
            @PathVariable Long id,
            @ModelAttribute LocationRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            LocationResponse updated = inventoryService.updateLocation(id, request);
            redirectAttributes.addFlashAttribute("flashMessage",
                "Ubicación '" + updated.getName() + "' actualizada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al actualizar ubicación: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/inventory/locations";
    }

    @PostMapping("/locations/{id}/delete")
    public String deleteLocation(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            inventoryService.deleteLocation(id);
            redirectAttributes.addFlashAttribute("flashMessage", "Ubicación eliminada correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al eliminar ubicación: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/inventory/locations";
    }

    @PostMapping("/stock/entry")
    public String addStockBatch(
            @ModelAttribute AddStockBatchRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "Familia";
            inventoryService.addStockBatch(request, username);
            redirectAttributes.addFlashAttribute("flashMessage", "Lote de stock ingresado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al ingresar lote: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/inventory";
    }

    @PostMapping("/stock/consume")
    public String consumeStock(
            @ModelAttribute ConsumeStockRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "Familia";
            inventoryService.consumeStock(request, username);
            redirectAttributes.addFlashAttribute("flashMessage", "Consumo registrado y deducido según política FEFO.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al registrar consumo: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/inventory";
    }

    @PostMapping("/stock/waste")
    public String registerWaste(
            @ModelAttribute RegisterWasteRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "Familia";
            inventoryService.registerWaste(request, username);
            redirectAttributes.addFlashAttribute("flashMessage", "Merma registrada y dada de baja del inventario.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al registrar merma: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/inventory";
    }

    @GetMapping("/movements")
    public String movements(
            @RequestParam(required = false) Long productId,
            Model model) {
        List<InventoryMovementResponse> movements = inventoryService.getMovements(productId);
        model.addAttribute("movements", movements);
        model.addAttribute("allProducts", catalogService.getAllProducts());
        model.addAttribute("selectedProduct", productId);
        return "pages/inventory/movements";
    }
}
