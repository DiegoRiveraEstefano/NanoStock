package cl.diego.NanoStock.demo.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.demo.config.DemoProperties;
import cl.diego.NanoStock.demo.dto.DemoStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoResetService {

    private final DemoProperties demoProperties;
    private final DemoDataSeeder demoDataSeeder;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public DemoStatusDto getStatus() {
        ensureDemoStateRow();
        return readStatus();
    }

    @Transactional
    public boolean cleanupIfDue() {
        if (!demoProperties.isEnabled()) {
            return false;
        }
        ensureDemoStateRow();
        DemoStatusDto status = readStatus();
        if (status.getNextResetAt() != null && !LocalDateTime.now().isBefore(status.getNextResetAt())) {
            log.info("Ventana de {} días cumplida (nextResetAt={}). Ejecutando limpieza automática del modo demo...",
                demoProperties.getResetIntervalDays(), status.getNextResetAt());
            cleanupNewDataAndReseed();
            return true;
        }
        return false;
    }

    @Transactional
    public DemoStatusDto cleanupNewDataAndReseed() {
        ensureDemoStateRow();

        int deleted = 0;
        // 1. Notificaciones nuevas
        deleted += jdbcTemplate.update("DELETE FROM nanostock_notifications WHERE is_seed = 0");

        // 2. Tablas analíticas dependientes de boletas, tiendas o productos no semilla
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_price_history
            WHERE receipt_id IN (SELECT id FROM nanostock_purchase_receipts WHERE is_seed = 0)
               OR product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
               OR store_id IN (SELECT id FROM nanostock_stores WHERE is_seed = 0)
            """);
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_product_consumption_metrics
            WHERE product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
            """);

        // 3. Listas de compra y sus ítems nuevos
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_shopping_list_items
            WHERE is_seed = 0
               OR shopping_list_id IN (SELECT id FROM nanostock_shopping_lists WHERE is_seed = 0)
               OR product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
            """);
        deleted += jdbcTemplate.update("DELETE FROM nanostock_shopping_lists WHERE is_seed = 0");

        // 4. Boletas de compra y sus ítems nuevos
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_purchase_receipt_items
            WHERE is_seed = 0
               OR receipt_id IN (SELECT id FROM nanostock_purchase_receipts WHERE is_seed = 0)
               OR product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
            """);
        deleted += jdbcTemplate.update("DELETE FROM nanostock_purchase_receipts WHERE is_seed = 0");

        // 5. Movimientos y lotes de inventario nuevos (incluyendo dependencias de productos/ubicaciones nuevas)
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_inventory_movements
            WHERE is_seed = 0
               OR product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
               OR location_id IN (SELECT id FROM nanostock_locations WHERE is_seed = 0)
            """);
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_stock_batches
            WHERE is_seed = 0
               OR product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
               OR location_id IN (SELECT id FROM nanostock_locations WHERE is_seed = 0)
            """);

        // 6. Recetas e ingredientes nuevos
        deleted += jdbcTemplate.update("""
            DELETE FROM nanostock_recipe_ingredients
            WHERE is_seed = 0
               OR recipe_id IN (SELECT id FROM nanostock_recipes WHERE is_seed = 0)
               OR product_id IN (SELECT id FROM nanostock_products WHERE is_seed = 0)
            """);
        deleted += jdbcTemplate.update("DELETE FROM nanostock_recipes WHERE is_seed = 0");

        // 7. Ubicaciones, tiendas, productos, marcas y categorías nuevas
        deleted += jdbcTemplate.update("DELETE FROM nanostock_locations WHERE is_seed = 0");
        deleted += jdbcTemplate.update("DELETE FROM nanostock_stores WHERE is_seed = 0");
        deleted += jdbcTemplate.update("DELETE FROM nanostock_products WHERE is_seed = 0");
        deleted += jdbcTemplate.update("DELETE FROM nanostock_brands WHERE is_seed = 0");
        deleted += jdbcTemplate.update("DELETE FROM nanostock_categories WHERE is_seed = 0");

        // 8. Usuarios no semilla (preservando cuentas ROLE_ADMIN del propietario del portafolio)
        deleted += jdbcTemplate.update("DELETE FROM nanostock_iam_users WHERE is_seed = 0 AND role <> 'ROLE_ADMIN'");

        // 9. Publicaciones de eventos completadas
        jdbcTemplate.update("DELETE FROM event_publication WHERE completion_date IS NOT NULL");

        // 10. Restaurar cualquier registro semilla faltante
        if (demoProperties.isSeedOnStartup() || demoProperties.isEnabled()) {
            demoDataSeeder.ensureSeedData();
        }

        LocalDateTime now = LocalDateTime.now();
        int intervalDays = Math.max(1, demoProperties.getResetIntervalDays());
        LocalDateTime nextReset = now.plusDays(intervalDays);

        jdbcTemplate.update("""
            UPDATE nanostock_demo_state
            SET last_reset_at = ?, next_reset_at = ?, last_cleanup_deleted_rows = ?, updated_at = ?
            WHERE id = 1
            """, now.toString(), nextReset.toString(), deleted, now.toString());

        log.info("Limpieza de modo demo completada: {} registros nuevos eliminados. Próxima limpieza programada para {}.",
            deleted, nextReset);

        return readStatus();
    }

    private void ensureDemoStateRow() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nanostock_demo_state WHERE id = 1", Integer.class);
        if (count == null || count == 0) {
            LocalDateTime now = LocalDateTime.now();
            int intervalDays = Math.max(1, demoProperties.getResetIntervalDays());
            LocalDateTime nextReset = now.plusDays(intervalDays);
            jdbcTemplate.update("""
                INSERT INTO nanostock_demo_state (id, last_reset_at, next_reset_at, last_cleanup_deleted_rows, updated_at)
                VALUES (1, ?, ?, 0, ?)
                """, now.toString(), nextReset.toString(), now.toString());
        }
    }

    private DemoStatusDto readStatus() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT last_reset_at, next_reset_at, last_cleanup_deleted_rows FROM nanostock_demo_state WHERE id = 1");
        if (rows.isEmpty()) {
            return DemoStatusDto.builder()
                .enabled(demoProperties.isEnabled())
                .resetIntervalDays(demoProperties.getResetIntervalDays())
                .demoUsername(demoProperties.getDemoUsername())
                .build();
        }

        Map<String, Object> row = rows.get(0);
        LocalDateTime lastResetAt = parseDateTime(row.get("last_reset_at"));
        LocalDateTime nextResetAt = parseDateTime(row.get("next_reset_at"));
        int deletedRows = row.get("last_cleanup_deleted_rows") instanceof Number n ? n.intValue() : 0;

        long hoursRemaining = 0;
        long daysRemaining = 0;
        if (nextResetAt != null) {
            Duration remaining = Duration.between(LocalDateTime.now(), nextResetAt);
            if (!remaining.isNegative()) {
                hoursRemaining = remaining.toHours();
                daysRemaining = (long) Math.ceil(hoursRemaining / 24.0);
            }
        }

        return DemoStatusDto.builder()
            .enabled(demoProperties.isEnabled())
            .resetIntervalDays(demoProperties.getResetIntervalDays())
            .lastResetAt(lastResetAt)
            .nextResetAt(nextResetAt)
            .daysRemaining(daysRemaining)
            .hoursRemaining(hoursRemaining)
            .lastCleanupDeletedRows(deletedRows)
            .demoUsername(demoProperties.getDemoUsername())
            .build();
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        String raw = value.toString().trim();
        if (raw.isEmpty()) return null;
        return LocalDateTime.parse(raw.replace(' ', 'T'));
    }
}
