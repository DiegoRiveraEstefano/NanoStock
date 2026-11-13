package cl.diego.NanoStock.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Brand;
import cl.diego.NanoStock.catalog.model.Category;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.BrandRepository;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.demo.config.DemoProperties;
import cl.diego.NanoStock.iam.model.Role;
import cl.diego.NanoStock.iam.model.User;
import cl.diego.NanoStock.iam.repository.UserRepository;
import cl.diego.NanoStock.inventory.model.Location;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.notifications.model.Notification;
import cl.diego.NanoStock.notifications.model.NotificationSeverity;
import cl.diego.NanoStock.notifications.model.NotificationType;
import cl.diego.NanoStock.notifications.repository.NotificationRepository;
import cl.diego.NanoStock.purchases.model.ShoppingItemStatus;
import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.model.ShoppingListItem;
import cl.diego.NanoStock.purchases.model.ShoppingListStatus;
import cl.diego.NanoStock.purchases.model.Store;
import cl.diego.NanoStock.purchases.model.StoreType;
import cl.diego.NanoStock.purchases.repository.ShoppingListRepository;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.Recipe;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import cl.diego.NanoStock.recipes.model.RecipeIngredient;
import cl.diego.NanoStock.recipes.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder {

    private final DemoProperties demoProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final StockBatchRepository stockBatchRepository;
    private final StoreRepository storeRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final RecipeRepository recipeRepository;
    private final NotificationRepository notificationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void ensureSeedData() {
        ensureDemoUser();

        Category lacteos = ensureCategory("Lácteos y Huevos", "Leches, quesos, yogures y huevos");
        Category despensaSeca = ensureCategory("Despensa Seca", "Arroz, pastas, harinas y aceites");
        Category carnes = ensureCategory("Carnes y Proteínas", "Carnes frescas y congeladas");
        Category verduras = ensureCategory("Verduras y Frutas", "Productos frescos de feria");

        Brand colun = ensureBrand("Colun");
        Brand tucapel = ensureBrand("Tucapel");
        Brand marcaPropia = ensureBrand("Marca Hogar");

        Location despensa = ensureLocation("Despensa Principal", StorageType.AMBIENT, "Estante principal de alimentos secos");
        Location refri = ensureLocation("Refrigerador Cocina", StorageType.REFRIGERATED, "Bandeja central y puerta");
        Location freezer = ensureLocation("Congelador Vertical", StorageType.FROZEN, "Cajón superior de carnes");

        Store jumbo = ensureStore("Supermercado Jumbo", "Sucursal Centro", StoreType.SUPERMARKET, "Av. Principal 1200");
        ensureStore("Feria Libre Local", "Puesto 14", StoreType.FARMERS_MARKET, "Plaza del Barrio");

        Product leche = ensureProduct("Leche Descremada 1L", "Leche UHT descremada en caja", "DEMO-LEC-001", "7802900001011",
            lacteos, colun, UnitOfMeasure.LITER, 2.0, 6.0, true);
        Product arroz = ensureProduct("Arroz Grano Largo 1kg", "Arroz grado 1 para preparación diaria", "DEMO-ARR-002", "7802900002025",
            despensaSeca, tucapel, UnitOfMeasure.KILOGRAM, 1.0, 4.0, false);
        Product huevos = ensureProduct("Huevos Blancos 12un", "Bandeja de 12 huevos grandes", "DEMO-HUE-003", "7802900003039",
            lacteos, marcaPropia, UnitOfMeasure.UNIT, 1.0, 2.0, true);
        Product pollo = ensureProduct("Pechuga de Pollo 1kg", "Pechuga deshuesada congelada", "DEMO-POL-004", "7802900004043",
            carnes, marcaPropia, UnitOfMeasure.KILOGRAM, 1.0, 3.0, true);
        Product tomate = ensureProduct("Tomate Fresco", "Tomate de ensalada maduro", "DEMO-TOM-005", "7802900005057",
            verduras, marcaPropia, UnitOfMeasure.KILOGRAM, 1.0, 3.0, true);
        Product aceite = ensureProduct("Aceite de Oliva 500ml", "Aceite extra virgen primera prensada", "DEMO-ACE-006", "7802900006061",
            despensaSeca, marcaPropia, UnitOfMeasure.MILLILITER, 1.0, 2.0, false);

        if (countSeedRows("nanostock_stock_batches") == 0) {
            seedBatch(leche, refri, 2.0, LocalDate.now().plusDays(2), SealedStatus.OPEN, "SEED-LEC-A");
            seedBatch(leche, refri, 3.0, LocalDate.now().plusDays(25), SealedStatus.SEALED, "SEED-LEC-B");
            seedBatch(huevos, refri, 1.0, LocalDate.now().plusDays(5), SealedStatus.SEALED, "SEED-HUE-A");
            seedBatch(arroz, despensa, 3.0, LocalDate.now().plusDays(180), SealedStatus.SEALED, "SEED-ARR-A");
            seedBatch(pollo, freezer, 2.0, LocalDate.now().plusDays(45), SealedStatus.SEALED, "SEED-POL-A");
            seedBatch(tomate, refri, 0.5, LocalDate.now().plusDays(3), SealedStatus.SEALED, "SEED-TOM-A");
        }

        if (countSeedRows("nanostock_recipes") == 0) {
            seedRecipe(
                "Arroz con Pollo Casero",
                "Receta clásica familiar que aprovecha pollo, arroz y tomate fresco.",
                "1. Dorar la pechuga de pollo en trozos.\n2. Sofreír tomate picado.\n3. Agregar arroz y agua hirviendo.\n4. Cocinar a fuego bajo por 20 minutos.",
                4, 35, RecipeDifficulty.EASY, RecipeCategory.LUNCH,
                new Object[][] {
                    {arroz, 0.5, UnitOfMeasure.KILOGRAM, false},
                    {pollo, 0.5, UnitOfMeasure.KILOGRAM, false},
                    {tomate, 0.3, UnitOfMeasure.KILOGRAM, true}
                }
            );
            seedRecipe(
                "Omelette de Tomate",
                "Desayuno o cena rápida ideal para aprovechar huevos próximos a vencer.",
                "1. Batir los huevos con una pizca de sal.\n2. Saltear tomate en cubos.\n3. Cuajar en sartén por 4 minutos.",
                2, 10, RecipeDifficulty.EASY, RecipeCategory.BREAKFAST,
                new Object[][] {
                    {huevos, 4.0, UnitOfMeasure.UNIT, false},
                    {tomate, 0.2, UnitOfMeasure.KILOGRAM, true}
                }
            );
        }

        if (countSeedRows("nanostock_shopping_lists") == 0) {
            ShoppingList list = ShoppingList.builder()
                .title("Reposición Semanal Demo")
                .status(ShoppingListStatus.ACTIVE)
                .targetStore(jumbo)
                .build();
            list.addItem(ShoppingListItem.builder()
                .product(aceite)
                .suggestedQuantity(1.0)
                .isAutoSuggested(true)
                .itemStatus(ShoppingItemStatus.PENDING)
                .unitPriceEstimate(new BigDecimal("4990.00"))
                .build());
            list.addItem(ShoppingListItem.builder()
                .product(tomate)
                .suggestedQuantity(2.0)
                .isAutoSuggested(true)
                .itemStatus(ShoppingItemStatus.PENDING)
                .unitPriceEstimate(new BigDecimal("1890.00"))
                .build());
            ShoppingList savedList = shoppingListRepository.save(list);
            markSeed("nanostock_shopping_lists", savedList.getId());
            savedList.getItems().forEach(item -> markSeed("nanostock_shopping_list_items", item.getId()));
        }

        if (countSeedRows("nanostock_notifications") == 0) {
            Notification welcome = notificationRepository.save(Notification.builder()
                .title("Modo Demo Portafolio Activo")
                .message("Puedes crear productos, lotes, ubicaciones o recetas libremente. Todos los datos nuevos se reinician automáticamente cada "
                    + demoProperties.getResetIntervalDays() + " días.")
                .notificationType(NotificationType.SYSTEM)
                .severity(NotificationSeverity.INFO)
                .isRead(false)
                .build());
            markSeed("nanostock_notifications", welcome.getId());
        }

        log.debug("Datos semilla del modo demo verificados/asegurados correctamente.");
    }

    private void ensureDemoUser() {
        String username = demoProperties.getDemoUsername().trim();
        String email = demoProperties.getDemoEmail().trim().toLowerCase();

        User user = userRepository.findByUsername(username)
            .or(() -> userRepository.findByEmail(email))
            .orElseGet(() -> userRepository.save(User.builder()
                .username(username)
                .email(email)
                .hashedPassword(passwordEncoder.encode(demoProperties.getDemoPassword()))
                .role(Role.ROLE_MEMBER)
                .enabled(true)
                .build()));

        markSeed("nanostock_iam_users", user.getId());
    }

    private Category ensureCategory(String name, String description) {
        Category category = categoryRepository.findByName(name)
            .orElseGet(() -> categoryRepository.save(Category.builder()
                .name(name)
                .description(description)
                .build()));
        markSeed("nanostock_categories", category.getId());
        return category;
    }

    private Brand ensureBrand(String name) {
        Brand brand = brandRepository.findByName(name)
            .orElseGet(() -> brandRepository.save(Brand.builder()
                .name(name)
                .build()));
        markSeed("nanostock_brands", brand.getId());
        return brand;
    }

    private Location ensureLocation(String name, StorageType type, String description) {
        Location location = locationRepository.findByNameIgnoreCase(name)
            .orElseGet(() -> locationRepository.save(Location.builder()
                .name(name)
                .storageType(type)
                .description(description)
                .build()));
        markSeed("nanostock_locations", location.getId());
        return location;
    }

    private Store ensureStore(String name, String branchName, StoreType type, String address) {
        Store store = storeRepository.findByName(name)
            .orElseGet(() -> storeRepository.save(Store.builder()
                .name(name)
                .branchName(branchName)
                .storeType(type)
                .address(address)
                .build()));
        markSeed("nanostock_stores", store.getId());
        return store;
    }

    private Product ensureProduct(
            String name,
            String description,
            String sku,
            String barCode,
            Category category,
            Brand brand,
            UnitOfMeasure unit,
            double minThreshold,
            double targetStock,
            boolean perishable) {
        Product product = productRepository.findBySku(sku)
            .orElseGet(() -> productRepository.save(Product.builder()
                .name(name)
                .description(description)
                .sku(sku)
                .barCode(barCode)
                .category(category)
                .brand(brand)
                .unitOfMeasure(unit)
                .minStockThreshold(minThreshold)
                .targetStockQuantity(targetStock)
                .isPerishable(perishable)
                .build()));
        markSeed("nanostock_products", product.getId());
        return product;
    }

    private void seedBatch(
            Product product,
            Location location,
            double quantity,
            LocalDate expirationDate,
            SealedStatus sealedStatus,
            String batchCode) {
        StockBatch batch = stockBatchRepository.save(StockBatch.builder()
            .product(product)
            .location(location)
            .quantity(quantity)
            .expirationDate(expirationDate)
            .sealedStatus(sealedStatus)
            .batchCode(batchCode)
            .build());
        markSeed("nanostock_stock_batches", batch.getId());
    }

    private void seedRecipe(
            String title,
            String description,
            String instructions,
            int servings,
            int prepMinutes,
            RecipeDifficulty difficulty,
            RecipeCategory category,
            Object[][] ingredientsData) {
        Recipe recipe = Recipe.builder()
            .title(title)
            .description(description)
            .instructions(instructions)
            .servings(servings)
            .portionType(PortionType.SCALABLE)
            .prepTimeMinutes(prepMinutes)
            .difficulty(difficulty)
            .category(category)
            .build();

        for (Object[] row : ingredientsData) {
            recipe.addIngredient(RecipeIngredient.builder()
                .product((Product) row[0])
                .quantity((Double) row[1])
                .unitOfMeasure((UnitOfMeasure) row[2])
                .isOptional((Boolean) row[3])
                .build());
        }

        Recipe saved = recipeRepository.save(recipe);
        markSeed("nanostock_recipes", saved.getId());
        saved.getIngredients().forEach(ing -> markSeed("nanostock_recipe_ingredients", ing.getId()));
    }

    private int countSeedRows(String table) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE is_seed = 1", Integer.class);
        return count != null ? count : 0;
    }

    private void markSeed(String table, Long id) {
        if (id != null) {
            jdbcTemplate.update("UPDATE " + table + " SET is_seed = 1 WHERE id = ?", id);
        }
    }
}
