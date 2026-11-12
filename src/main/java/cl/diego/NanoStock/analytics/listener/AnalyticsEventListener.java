package cl.diego.NanoStock.analytics.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.analytics.service.ConsumptionAnalyticsService;
import cl.diego.NanoStock.analytics.service.PriceAnalyticsService;
import cl.diego.NanoStock.inventory.event.StockConsumedEvent;
import cl.diego.NanoStock.inventory.event.StockLevelChangedEvent;
import cl.diego.NanoStock.purchases.event.PurchaseCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventListener {

    private final PriceAnalyticsService priceAnalyticsService;
    private final ConsumptionAnalyticsService consumptionAnalyticsService;

    @EventListener
    @Transactional
    public void onPurchaseCompleted(PurchaseCompletedEvent event) {
        log.info("Registrando historial de precios desde boleta #{} (tienda ID: {})", event.receiptId(), event.storeId());

        if (event.items() == null) {
            return;
        }

        for (PurchaseCompletedEvent.PurchaseItemDetail item : event.items()) {
            if (item.productId() != null && item.unitPrice() != null) {
                try {
                    priceAnalyticsService.recordPrice(
                        item.productId(),
                        event.storeId(),
                        item.unitPrice(),
                        event.purchaseDate(),
                        event.receiptId()
                    );
                } catch (Exception ex) {
                    log.error("Error al registrar precio para producto ID {}: {}", item.productId(), ex.getMessage());
                }
            }
        }
    }

    @EventListener
    @Transactional
    public void onStockConsumed(StockConsumedEvent event) {
        log.info("Recalculando métricas de consumo por consumo de producto ID: {}", event.productId());
        try {
            consumptionAnalyticsService.calculateAndSaveMetrics(event.productId(), 30);
        } catch (Exception ex) {
            log.error("Error al recalcular métricas de consumo para producto ID {}: {}", event.productId(), ex.getMessage());
        }
    }

    @EventListener
    @Transactional
    public void onStockLevelChanged(StockLevelChangedEvent event) {
        log.debug("Actualizando autonomía de producto ID: {}", event.productId());
        try {
            consumptionAnalyticsService.calculateAndSaveMetrics(event.productId(), 30);
        } catch (Exception ex) {
            log.error("Error al recalcular autonomía para producto ID {}: {}", event.productId(), ex.getMessage());
        }
    }
}
