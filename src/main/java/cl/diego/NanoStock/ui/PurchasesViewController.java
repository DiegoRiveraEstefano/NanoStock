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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.purchases.dto.AddShoppingItemRequest;
import cl.diego.NanoStock.purchases.dto.RecordReceiptRequest;
import cl.diego.NanoStock.purchases.dto.ShoppingListItemResponse;
import cl.diego.NanoStock.purchases.dto.ShoppingListResponse;
import cl.diego.NanoStock.purchases.dto.StoreRequest;
import cl.diego.NanoStock.purchases.dto.UpdateShoppingItemStatusRequest;
import cl.diego.NanoStock.purchases.model.ShoppingItemStatus;
import cl.diego.NanoStock.purchases.service.PurchaseService;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import cl.diego.NanoStock.purchases.service.StoreService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchasesViewController {

    private final ShoppingListService shoppingListService;
    private final StoreService storeService;
    private final PurchaseService purchaseService;
    private final ProductCatalogService catalogService;
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @GetMapping("/shopping-list")
    public String shoppingList(
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {

        var activeList = shoppingListService.getOrCreateActiveList();
        ShoppingListResponse listResponse = shoppingListService.toShoppingListResponse(activeList);

        model.addAttribute("shoppingList", listResponse);
        model.addAttribute("allProducts", catalogService.getAllProducts());
        model.addAttribute("flashMessage", flashMessage);
        model.addAttribute("flashType", flashType);

        return "pages/purchases/shopping_list";
    }

    @GetMapping("/supermarket")
    public String supermarketMode(Model model) {
        var activeList = shoppingListService.getOrCreateActiveList();
        ShoppingListResponse listResponse = shoppingListService.toShoppingListResponse(activeList);

        model.addAttribute("shoppingList", listResponse);
        return "pages/purchases/supermarket_mode";
    }

    @PostMapping("/shopping-lists/suggest")
    public String suggestMissingItems(RedirectAttributes redirectAttributes) {
        try {
            var activeList = shoppingListService.getOrCreateActiveList();
            shoppingListService.generateSuggestedItems(activeList.getId());
            redirectAttributes.addFlashAttribute("flashMessage", "Lista actualizada con sugerencias automáticas de productos bajo stock mínimo.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al generar sugerencias: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/purchases/shopping-list";
    }

    @PostMapping("/shopping-lists/items")
    public String addItem(
            @ModelAttribute AddShoppingItemRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            var activeList = shoppingListService.getOrCreateActiveList();
            shoppingListService.addItem(activeList.getId(), request);
            redirectAttributes.addFlashAttribute("flashMessage", "Producto añadido a la lista.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al añadir ítem: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/purchases/shopping-list";
    }

    @PostMapping("/shopping-lists/items/{itemId}/status")
    @ResponseBody
    public ShoppingListItemResponse updateItemStatus(
            @PathVariable Long itemId,
            @RequestParam ShoppingItemStatus status) {
        return shoppingListService.updateItemStatus(itemId, UpdateShoppingItemStatusRequest.builder()
                .itemStatus(status)
                .build());
    }

    @PostMapping("/shopping-lists/items/{itemId}/delete")
    public String deleteItem(
            @PathVariable Long itemId,
            RedirectAttributes redirectAttributes) {
        shoppingListService.deleteItem(itemId);
        redirectAttributes.addFlashAttribute("flashMessage", "Ítem removido de la lista.");
        return "redirect:/purchases/shopping-list";
    }

    @GetMapping("/receipts/new")
    public String newReceiptForm(Model model) throws JsonProcessingException {
        var products = catalogService.getAllProducts();
        var locations = inventoryService.getAllLocations();

        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("products", products);
        model.addAttribute("locations", locations);
        model.addAttribute("productsJson", objectMapper.writeValueAsString(products));
        model.addAttribute("locationsJson", objectMapper.writeValueAsString(locations));

        return "pages/purchases/receipt_form";
    }

    @PostMapping("/receipts")
    public String recordReceipt(
            @ModelAttribute RecordReceiptRequest request,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "Familia";
            purchaseService.recordReceipt(request, username);
            redirectAttributes.addFlashAttribute("flashMessage", "Boleta de compra registrada exitosamente y stock ingresado al inventario.");
            return "redirect:/inventory";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al procesar la boleta: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
            return "redirect:/purchases/receipts/new";
        }
    }

    @GetMapping("/stores")
    public String listStores(
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {
        model.addAttribute("stores", storeService.getAllStores());
        model.addAttribute("flashMessage", flashMessage);
        model.addAttribute("flashType", flashType);
        return "pages/purchases/stores";
    }

    @PostMapping("/stores")
    public String createStore(
            @ModelAttribute StoreRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            storeService.createStore(request);
            redirectAttributes.addFlashAttribute("flashMessage", "Comercio registrado correctamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al registrar comercio: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/purchases/stores";
    }
}
