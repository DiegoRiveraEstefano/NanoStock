package cl.diego.NanoStock.inventory.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.model.Location;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.purchases.event.PurchaseCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseEventListener {

    private final InventoryService inventoryService;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;

    @EventListener
    @Transactional
    public void onPurchaseCompleted(PurchaseCompletedEvent event) {
        log.info("Procesando entrada de inventario por boleta de compra #{} de tienda {}",
            event.receiptId(), event.storeName());

        Location defaultAmbient = getOrCreateDefaultLocation("Despensa Principal", StorageType.AMBIENT);
        Location defaultRefrig = getOrCreateDefaultLocation("Refrigerador", StorageType.REFRIGERATED);

        for (PurchaseCompletedEvent.PurchaseItemDetail item : event.items()) {
            Long targetLocationId = item.locationId();

            if (targetLocationId == null) {
                // Si no se especificó ubicación, decidir según si el producto es perecedero o la categoría
                Product product = productRepository.findById(item.productId()).orElse(null);
                if (product != null && product.isPerishable() &&
                    product.getCategory() != null &&
                    (product.getCategory().getName().toLowerCase().contains("lácteo") ||
                     product.getCategory().getName().toLowerCase().contains("carne") ||
                     product.getCategory().getName().toLowerCase().contains("fresco"))) {
                    targetLocationId = defaultRefrig.getId();
                } else {
                    targetLocationId = defaultAmbient.getId();
                }
            }

            AddStockBatchRequest batchRequest = AddStockBatchRequest.builder()
                .productId(item.productId())
                .locationId(targetLocationId)
                .quantity(item.quantity())
                .expirationDate(item.expirationDate())
                .sealedStatus(SealedStatus.SEALED)
                .batchCode("COMPRA-" + event.receiptId())
                .notes("Ingreso automático por boleta #" + event.receiptId() + " (" + event.storeName() + ")")
                .build();

            inventoryService.addStockBatch(batchRequest, event.username());
        }
    }

    private Location getOrCreateDefaultLocation(String name, StorageType type) {
        return locationRepository.findByName(name)
            .orElseGet(() -> locationRepository.save(
                Location.builder()
                    .name(name)
                    .storageType(type)
                    .description("Ubicación por defecto del sistema")
                    .build()
            ));
    }
}
