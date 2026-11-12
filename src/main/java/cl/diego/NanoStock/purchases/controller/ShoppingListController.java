package cl.diego.NanoStock.purchases.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.purchases.dto.AddShoppingItemRequest;
import cl.diego.NanoStock.purchases.dto.CreateShoppingListRequest;
import cl.diego.NanoStock.purchases.dto.ShoppingListItemResponse;
import cl.diego.NanoStock.purchases.dto.ShoppingListResponse;
import cl.diego.NanoStock.purchases.dto.UpdateShoppingItemStatusRequest;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @PostMapping
    public ResponseEntity<ShoppingListResponse> createList(@Valid @RequestBody CreateShoppingListRequest request) {
        ShoppingListResponse created = shoppingListService.createList(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ShoppingListResponse>> getAllLists() {
        return ResponseEntity.ok(shoppingListService.getAllLists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListResponse> getListById(@PathVariable Long id) {
        return shoppingListService.getListById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/active/generate-suggestions")
    public ResponseEntity<ShoppingListResponse> generateSuggestions(
            @RequestParam(required = false) Long listId) {
        return ResponseEntity.ok(shoppingListService.generateSuggestedItems(listId));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ShoppingListItemResponse> addItem(
            @PathVariable Long id,
            @Valid @RequestBody AddShoppingItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.addItem(id, request));
    }

    @PutMapping("/items/{itemId}/status")
    public ResponseEntity<ShoppingListItemResponse> updateItemStatus(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateShoppingItemStatusRequest request) {
        return ResponseEntity.ok(shoppingListService.updateItemStatus(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        shoppingListService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ShoppingListResponse> completeList(@PathVariable Long id) {
        return ResponseEntity.ok(shoppingListService.completeList(id));
    }
}
