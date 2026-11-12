package cl.diego.NanoStock.purchases.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.iam.model.User;
import cl.diego.NanoStock.iam.repository.UserRepository;
import cl.diego.NanoStock.purchases.dto.ReceiptItemRequest;
import cl.diego.NanoStock.purchases.dto.ReceiptItemResponse;
import cl.diego.NanoStock.purchases.dto.ReceiptResponse;
import cl.diego.NanoStock.purchases.dto.RecordReceiptRequest;
import cl.diego.NanoStock.purchases.event.PurchaseCompletedEvent;
import cl.diego.NanoStock.purchases.model.PaymentMethod;
import cl.diego.NanoStock.purchases.model.Receipt;
import cl.diego.NanoStock.purchases.model.ReceiptItem;
import cl.diego.NanoStock.purchases.model.Store;
import cl.diego.NanoStock.purchases.repository.ReceiptRepository;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final ReceiptRepository receiptRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReceiptResponse recordReceipt(RecordReceiptRequest request, String username) {
        Store store = storeRepository.findById(request.getStoreId())
            .orElseThrow(() -> new IllegalArgumentException("Comercio no encontrado con ID: " + request.getStoreId()));

        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;

        Receipt receipt = Receipt.builder()
            .store(store)
            .purchaseDate(request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDate.now())
            .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.DEBIT_CARD)
            .notes(request.getNotes())
            .user(user)
            .build();

        BigDecimal grandTotal = BigDecimal.ZERO;
        List<PurchaseCompletedEvent.PurchaseItemDetail> eventItems = new ArrayList<>();

        for (ReceiptItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + itemReq.getProductId()));

            BigDecimal qty = BigDecimal.valueOf(itemReq.getQuantity());
            BigDecimal grossLine = itemReq.getUnitPrice().multiply(qty);
            BigDecimal discount = itemReq.getDiscountAmount() != null ? itemReq.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal netLine = grossLine.subtract(discount).max(BigDecimal.ZERO);

            ReceiptItem receiptItem = ReceiptItem.builder()
                .product(product)
                .quantity(itemReq.getQuantity())
                .unitPrice(itemReq.getUnitPrice())
                .discountAmount(discount)
                .totalLineAmount(netLine)
                .build();

            receipt.addItem(receiptItem);
            grandTotal = grandTotal.add(netLine);

            eventItems.add(new PurchaseCompletedEvent.PurchaseItemDetail(
                product.getId(),
                product.getName(),
                itemReq.getQuantity(),
                itemReq.getUnitPrice(),
                discount,
                netLine,
                itemReq.getExpirationDate(),
                itemReq.getLocationId()
            ));
        }

        receipt.setTotalAmount(grandTotal);
        Receipt saved = receiptRepository.save(receipt);

        // Publicar evento de dominio para desacoplamiento (actualiza stock e histórico de precios)
        eventPublisher.publishEvent(new PurchaseCompletedEvent(
            saved.getId(),
            store.getId(),
            store.getName(),
            saved.getPurchaseDate(),
            saved.getTotalAmount(),
            eventItems,
            username
        ));

        return toReceiptResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<ReceiptResponse> getReceiptById(Long id) {
        return receiptRepository.findById(id).map(this::toReceiptResponse);
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getAllReceipts() {
        return receiptRepository.findAllByOrderByPurchaseDateDesc().stream()
            .map(this::toReceiptResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByDateRange(LocalDate from, LocalDate to) {
        return receiptRepository.findByPurchaseDateBetweenOrderByPurchaseDateDesc(from, to).stream()
            .map(this::toReceiptResponse)
            .toList();
    }

    public ReceiptResponse toReceiptResponse(Receipt receipt) {
        List<ReceiptItemResponse> items = receipt.getItems().stream()
            .map(item -> ReceiptItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .productBarCode(item.getProduct().getBarCode())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())
                .totalLineAmount(item.getTotalLineAmount())
                .build())
            .toList();

        return ReceiptResponse.builder()
            .id(receipt.getId())
            .storeId(receipt.getStore() != null ? receipt.getStore().getId() : null)
            .storeName(receipt.getStore() != null ? receipt.getStore().getName() : null)
            .storeBranch(receipt.getStore() != null ? receipt.getStore().getBranchName() : null)
            .purchaseDate(receipt.getPurchaseDate())
            .totalAmount(receipt.getTotalAmount())
            .paymentMethod(receipt.getPaymentMethod())
            .notes(receipt.getNotes())
            .username(receipt.getUser() != null ? receipt.getUser().getUsername() : null)
            .items(items)
            .createdAt(receipt.getCreatedAt())
            .build();
    }
}
