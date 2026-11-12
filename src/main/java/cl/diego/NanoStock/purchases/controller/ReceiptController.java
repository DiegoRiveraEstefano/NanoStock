package cl.diego.NanoStock.purchases.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.purchases.dto.ReceiptResponse;
import cl.diego.NanoStock.purchases.dto.RecordReceiptRequest;
import cl.diego.NanoStock.purchases.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchases/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<ReceiptResponse> recordReceipt(
            @Valid @RequestBody RecordReceiptRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        ReceiptResponse response = purchaseService.recordReceipt(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReceiptResponse>> getAllReceipts() {
        return ResponseEntity.ok(purchaseService.getAllReceipts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptResponse> getReceiptById(@PathVariable Long id) {
        return purchaseService.getReceiptById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
