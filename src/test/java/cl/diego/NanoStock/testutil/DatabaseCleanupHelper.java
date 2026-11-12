package cl.diego.NanoStock.testutil;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleanupHelper {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCleanupHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void cleanAll() {
        jdbcTemplate.execute("DELETE FROM nanostock_notifications");
        jdbcTemplate.execute("DELETE FROM nanostock_price_history");
        jdbcTemplate.execute("DELETE FROM nanostock_product_consumption_metrics");
        jdbcTemplate.execute("DELETE FROM nanostock_shopping_list_items");
        jdbcTemplate.execute("DELETE FROM nanostock_shopping_lists");
        jdbcTemplate.execute("DELETE FROM nanostock_purchase_receipt_items");
        jdbcTemplate.execute("DELETE FROM nanostock_purchase_receipts");
        jdbcTemplate.execute("DELETE FROM nanostock_inventory_movements");
        jdbcTemplate.execute("DELETE FROM nanostock_stock_batches");
        jdbcTemplate.execute("DELETE FROM nanostock_locations");
        jdbcTemplate.execute("DELETE FROM nanostock_recipe_ingredients");
        jdbcTemplate.execute("DELETE FROM nanostock_recipes");
        jdbcTemplate.execute("DELETE FROM nanostock_products");
        jdbcTemplate.execute("DELETE FROM nanostock_brands");
        jdbcTemplate.execute("DELETE FROM nanostock_categories");
        jdbcTemplate.execute("DELETE FROM nanostock_stores");
    }
}
