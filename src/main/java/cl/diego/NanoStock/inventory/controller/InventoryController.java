package cl.diego.NanoStock.inventory.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.ConsumeStockRequest;
import cl.diego.NanoStock.inventory.dto.InventoryMovementResponse;
import cl.diego.NanoStock.inventory.dto.ProductStockOverview;
import cl.diego.NanoStock.inventory.dto.RegisterWasteRequest;
import cl.diego.NanoStock.inventory.dto.StockAdjustmentRequest;
import cl.diego.NanoStock.inventory.dto.StockBatchResponse;
import cl.diego.NanoStock.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/add")
    public ResponseEntity<StockBatchResponse> addStock(
            @Valid @RequestBody AddStockBatchRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        StockBatchResponse response = inventoryService.addStockBatch(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/consume")
    public ResponseEntity<ProductStockOverview> consumeStock(
            @Valid @RequestBody ConsumeStockRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(inventoryService.consumeStock(request, username));
    }

    @PostMapping("/waste")
    public ResponseEntity<StockBatchResponse> registerWaste(
            @Valid @RequestBody RegisterWasteRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(inventoryService.registerWaste(request, username));
    }

    @PostMapping("/reconcile")
    public ResponseEntity<ProductStockOverview> reconcileStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(inventoryService.reconcileStock(request, username));
    }

    @GetMapping("/overview")
    public ResponseEntity<List<ProductStockOverview>> getAllStockOverview() {
        return ResponseEntity.ok(inventoryService.getAllStockOverview());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductStockOverview> getProductStockOverview(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStockOverview(productId));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<StockBatchResponse>> getExpiringBatches(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(inventoryService.getExpiringBatches(days));
    }

    @GetMapping("/low")
    public ResponseEntity<List<ProductStockOverview>> getLowStockProducts() {
        return ResponseEntity.ok(inventoryService.getLowStockProducts());
    }

    @GetMapping("/movements")
    public ResponseEntity<List<InventoryMovementResponse>> getMovements(
            @RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(inventoryService.getMovements(productId));
    }
}
