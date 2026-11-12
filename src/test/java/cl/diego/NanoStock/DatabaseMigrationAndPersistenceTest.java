package cl.diego.NanoStock;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;

@SpringBootTest
class DatabaseMigrationAndPersistenceTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Todas las migraciones de Flyway deben aplicarse exitosamente")
    void flywayMigrationsShouldApplySuccessfully() {
        var appliedMigrations = flyway.info().applied();
        assertThat(appliedMigrations).isNotEmpty();
        assertThat(appliedMigrations.length).isGreaterThanOrEqualTo(8);

        for (var migration : appliedMigrations) {
            assertThat(migration.getState().isApplied()).isTrue();
        }
    }

    @Test
    @DisplayName("Las tablas principales del ERP doméstico deben existir en la base de datos")
    void mainTablesShouldExistInDatabase() throws Exception {
        Set<String> tableNames = new HashSet<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        assertThat(tableNames).contains(
            "nanostock_iam_users",
            "nanostock_categories",
            "nanostock_brands",
            "nanostock_products",
            "nanostock_locations",
            "nanostock_stock_batches",
            "nanostock_inventory_movements",
            "nanostock_stores",
            "nanostock_purchase_receipts",
            "nanostock_purchase_receipt_items",
            "nanostock_shopping_lists",
            "nanostock_shopping_list_items",
            "nanostock_price_history",
            "nanostock_product_consumption_metrics",
            "nanostock_recipes",
            "nanostock_recipe_ingredients",
            "nanostock_notifications",
            "event_publication"
        );
    }

    @Test
    @DisplayName("Debe persistir y consultar un producto en SQLite")
    void shouldPersistAndRetrieveProduct() {
        Product product = Product.builder()
            .name("Leche Entera 1L")
            .description("Leche líquida UHT entera")
            .sku("LEC-ENT-001")
            .barCode("7801234567890")
            .build();

        Product saved = productRepository.save(product);
        assertThat(saved.getId()).isNotNull();

        Optional<Product> found = productRepository.findByBarCode("7801234567890");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Leche Entera 1L");

        productRepository.delete(saved);
    }
}
