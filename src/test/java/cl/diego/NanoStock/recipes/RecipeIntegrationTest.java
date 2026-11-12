package cl.diego.NanoStock.recipes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.purchases.dto.ShoppingListResponse;
import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import cl.diego.NanoStock.recipes.dto.CookRecipeResponse;
import cl.diego.NanoStock.recipes.dto.CreateRecipeRequest;
import cl.diego.NanoStock.recipes.dto.RecipeIngredientRequest;
import cl.diego.NanoStock.recipes.dto.RecipeMatchResponse;
import cl.diego.NanoStock.recipes.dto.RecipeResponse;
import cl.diego.NanoStock.recipes.dto.ScaledRecipeResponse;
import cl.diego.NanoStock.recipes.event.RecipeCookedEvent;
import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import cl.diego.NanoStock.recipes.repository.RecipeIngredientRepository;
import cl.diego.NanoStock.recipes.repository.RecipeRepository;
import cl.diego.NanoStock.recipes.service.RecipeMatchingService;
import cl.diego.NanoStock.recipes.service.RecipeService;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@RecordApplicationEvents
class RecipeIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeMatchingService matchingService;

    @Autowired
    private ProductCatalogService catalogService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeIngredientRepository ingredientRepository;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private DatabaseCleanupHelper cleanupHelper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        cleanupHelper.cleanAll();
    }

    @Test
    @WithMockUser(username = "mama", roles = {"MEMBER"})
    @DisplayName("Debe crear una receta escalable con notas culinarias e ingredientes vinculados al catálogo")
    void shouldCreateScalableRecipeWithNotes() {
        ProductResponse fideos = catalogService.createProduct(CreateProductRequest.builder()
            .name("Spaghetti N°5")
            .sku("SPA-05-400G")
            .barCode("7801230001111")
            .unitOfMeasure(UnitOfMeasure.PACKAGE)
            .build());

        ProductResponse salsa = catalogService.createProduct(CreateProductRequest.builder()
            .name("Salsa de Tomate Italiana 200g")
            .sku("SAL-TOM-200G")
            .barCode("7801230002222")
            .unitOfMeasure(UnitOfMeasure.PACKAGE)
            .build());

        RecipeResponse recipe = recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Tallarines con Salsa Casera")
            .description("Plato clásico familiar para el almuerzo")
            .instructions("Hervir agua con sal. Cocinar fideos 8 min. Calentar salsa y mezclar.")
            .servings(4)
            .portionType(PortionType.SCALABLE)
            .prepTimeMinutes(20)
            .difficulty(RecipeDifficulty.EASY)
            .category(RecipeCategory.LUNCH)
            .notes("Tip: Agregar una pizca de orégano seco y aceite de oliva al servir.")
            .ingredients(List.of(
                RecipeIngredientRequest.builder()
                    .productId(fideos.getId())
                    .quantity(1.0)
                    .unitOfMeasure(UnitOfMeasure.PACKAGE)
                    .notes("Al dente")
                    .build(),
                RecipeIngredientRequest.builder()
                    .productId(salsa.getId())
                    .quantity(2.0)
                    .unitOfMeasure(UnitOfMeasure.PACKAGE)
                    .notes("Calentar a fuego lento")
                    .build()
            ))
            .build());

        assertThat(recipe.getId()).isNotNull();
        assertThat(recipe.getTitle()).isEqualTo("Tallarines con Salsa Casera");
        assertThat(recipe.getServings()).isEqualTo(4);
        assertThat(recipe.getPortionType()).isEqualTo(PortionType.SCALABLE);
        assertThat(recipe.getNotes()).contains("orégano seco");
        assertThat(recipe.getIngredients()).hasSize(2);
    }

    @Test
    @WithMockUser(username = "papa", roles = {"MEMBER"})
    @DisplayName("Debe escalar cantidades de ingredientes dinámicamente según el número de comensales deseado")
    void shouldScaleIngredientsForDesiredPortions() {
        ProductResponse cafe = catalogService.createProduct(CreateProductRequest.builder()
            .name("Café de Grano")
            .sku("CAF-GRA-01")
            .barCode("7807788990011")
            .unitOfMeasure(UnitOfMeasure.GRAM)
            .build());

        ProductResponse leche = catalogService.createProduct(CreateProductRequest.builder()
            .name("Leche Entera")
            .sku("LEC-ENT-02")
            .barCode("7807788990022")
            .unitOfMeasure(UnitOfMeasure.MILLILITER)
            .build());

        // Receta configurada para 1 porción única (individual)
        RecipeResponse latte = recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Café Latte Individual")
            .instructions("Extraer espresso y vaporizar leche")
            .servings(1)
            .portionType(PortionType.FIXED_SINGLE)
            .ingredients(List.of(
                RecipeIngredientRequest.builder()
                    .productId(cafe.getId())
                    .quantity(15.0) // 15g por taza
                    .unitOfMeasure(UnitOfMeasure.GRAM)
                    .build(),
                RecipeIngredientRequest.builder()
                    .productId(leche.getId())
                    .quantity(200.0) // 200ml por taza
                    .unitOfMeasure(UnitOfMeasure.MILLILITER)
                    .build()
            ))
            .build());

        // Escalar para 3 personas en el desayuno
        ScaledRecipeResponse scaled = recipeService.scaleRecipe(latte.getId(), 3);
        assertThat(scaled.getTargetServings()).isEqualTo(3);
        assertThat(scaled.getScalingFactor()).isEqualTo(3.0);

        var cafeScaled = scaled.getIngredients().stream().filter(i -> i.getProductId().equals(cafe.getId())).findFirst().orElseThrow();
        var lecheScaled = scaled.getIngredients().stream().filter(i -> i.getProductId().equals(leche.getId())).findFirst().orElseThrow();

        assertThat(cafeScaled.getScaledQuantity()).isEqualTo(45.0); // 15g * 3 = 45g
        assertThat(lecheScaled.getScaledQuantity()).isEqualTo(600.0); // 200ml * 3 = 600ml
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Motor '¿Qué puedo cocinar hoy?': Sugiere recetas viables y prioriza las que usan ingredientes por vencer")
    void shouldSuggestRecipesAndPrioritizeExpiringIngredients() {
        LocationResponse despensa = inventoryService.createLocation(LocationRequest.builder()
            .name("Despensa")
            .storageType(StorageType.AMBIENT)
            .build());

        ProductResponse atun = catalogService.createProduct(CreateProductRequest.builder()
            .name("Lomitos de Atún")
            .sku("ATU-LOM-01")
            .barCode("7801122998877")
            .unitOfMeasure(UnitOfMeasure.UNIT)
            .build());

        ProductResponse arroz = catalogService.createProduct(CreateProductRequest.builder()
            .name("Arroz Grado 1")
            .sku("ARR-GRA-01")
            .barCode("7801122998888")
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .build());

        ProductResponse crema = catalogService.createProduct(CreateProductRequest.builder()
            .name("Crema Fresca")
            .sku("CRE-FRE-01")
            .barCode("7801122998899")
            .unitOfMeasure(UnitOfMeasure.UNIT)
            .build());

        // Lote de Atún vence en 2 días (¡Urgente consumir!)
        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(atun.getId())
            .locationId(despensa.getId())
            .quantity(2.0)
            .expirationDate(LocalDate.now().plusDays(2))
            .build(), "diego");

        // Lote de Arroz vence en 1 año
        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(arroz.getId())
            .locationId(despensa.getId())
            .quantity(5.0)
            .expirationDate(LocalDate.now().plusYears(1))
            .build(), "diego");

        // Receta 1: Arroz con Atún (Usa atún que vence pronto -> debe tener máxima prioridad)
        recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Arroz con Atún Express")
            .instructions("Cocinar arroz y mezclar con atún")
            .servings(2)
            .ingredients(List.of(
                RecipeIngredientRequest.builder().productId(arroz.getId()).quantity(0.5).build(),
                RecipeIngredientRequest.builder().productId(atun.getId()).quantity(2.0).build()
            ))
            .build());

        // Receta 2: Crema de Arroz (Falta crema en stock -> no se puede cocinar completa)
        recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Postre Crema de Arroz")
            .instructions("Hervir arroz con crema y azúcar")
            .servings(2)
            .ingredients(List.of(
                RecipeIngredientRequest.builder().productId(arroz.getId()).quantity(0.5).build(),
                RecipeIngredientRequest.builder().productId(crema.getId()).quantity(1.0).build()
            ))
            .build());

        List<RecipeMatchResponse> suggestions = matchingService.findMatchableRecipes(2, null);

        assertThat(suggestions).hasSize(2);

        // La receta 1 debe estar primera: se puede cocinar ahora y usa un ingrediente pronto a caducar
        RecipeMatchResponse topSuggestion = suggestions.get(0);
        assertThat(topSuggestion.getRecipeTitle()).isEqualTo("Arroz con Atún Express");
        assertThat(topSuggestion.isCanCookNow()).isTrue();
        assertThat(topSuggestion.getViabilityPercentage()).isEqualTo(100.0);
        assertThat(topSuggestion.getExpiringIngredientsCount()).isEqualTo(1);

        // La receta 2 debe reportar ingrediente faltante
        RecipeMatchResponse secondSuggestion = suggestions.get(1);
        assertThat(secondSuggestion.getRecipeTitle()).isEqualTo("Postre Crema de Arroz");
        assertThat(secondSuggestion.isCanCookNow()).isFalse();
        assertThat(secondSuggestion.getMissingIngredients()).hasSize(1);
        assertThat(secondSuggestion.getMissingIngredients().get(0).getProductName()).isEqualTo("Crema Fresca");
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe cocinar una receta y descontar de forma atómica los ingredientes del inventario vía FEFO")
    void shouldCookRecipeAndDeductInventory() {
        LocationResponse despensa = inventoryService.createLocation(LocationRequest.builder()
            .name("Despensa")
            .build());

        ProductResponse huevos = catalogService.createProduct(CreateProductRequest.builder()
            .name("Huevos Color")
            .sku("HUE-COL-01")
            .barCode("7803334445556")
            .unitOfMeasure(UnitOfMeasure.UNIT)
            .build());

        inventoryService.addStockBatch(AddStockBatchRequest.builder()
            .productId(huevos.getId())
            .locationId(despensa.getId())
            .quantity(6.0)
            .build(), "diego");

        RecipeResponse omelette = recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Omelette Francés")
            .instructions("Batir huevos con sal y dorar en sartén con mantequilla")
            .servings(1)
            .portionType(PortionType.FIXED_SINGLE)
            .ingredients(List.of(
                RecipeIngredientRequest.builder()
                    .productId(huevos.getId())
                    .quantity(2.0)
                    .build()
            ))
            .build());

        // Cocinar 2 raciones (utilizará 4 huevos en total)
        CookRecipeResponse cookResponse = recipeService.cookRecipe(omelette.getId(), 2, "diego");

        assertThat(cookResponse.getPortionsCooked()).isEqualTo(2);
        assertThat(cookResponse.getConsumedIngredients()).hasSize(1);
        assertThat(cookResponse.getConsumedIngredients().get(0).getQuantityConsumed()).isEqualTo(4.0);

        // Validar stock restante en inventario: 6 - 4 = 2 huevos disponibles
        Double remainingStock = stockBatchRepository.sumQuantityByProductId(huevos.getId());
        assertThat(remainingStock).isEqualTo(2.0);

        // Validar publicación del evento de dominio
        long cookedEvents = applicationEvents.stream(RecipeCookedEvent.class)
            .filter(e -> e.recipeId().equals(omelette.getId()) && e.portionsCooked() == 2)
            .count();
        assertThat(cookedEvents).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("Debe agregar ingredientes faltantes de una receta a la lista de compras activa")
    void shouldAddMissingRecipeIngredientsToActiveShoppingList() {
        ProductResponse harina = catalogService.createProduct(CreateProductRequest.builder()
            .name("Harina Sin Polvos")
            .sku("HAR-01")
            .barCode("7802223334445")
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .build());

        // No hay stock de harina (0.0 disponible)
        RecipeResponse pan = recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Pan Casero")
            .instructions("Amasar y hornear")
            .servings(4)
            .ingredients(List.of(
                RecipeIngredientRequest.builder()
                    .productId(harina.getId())
                    .quantity(1.0)
                    .build()
            ))
            .build());

        int added = recipeService.addMissingIngredientsToShoppingList(pan.getId(), 4);
        assertThat(added).isEqualTo(1);

        ShoppingList activeListEntity = shoppingListService.getOrCreateActiveList();
        ShoppingListResponse activeList = shoppingListService.getListById(activeListEntity.getId()).orElseThrow();
        assertThat(activeList.getItems()).hasSize(1);
        assertThat(activeList.getItems().get(0).getProductName()).isEqualTo("Harina Sin Polvos");
        assertThat(activeList.getItems().get(0).getSuggestedQuantity()).isEqualTo(1.0);
    }

    @Test
    @WithMockUser(username = "diego", roles = {"MEMBER"})
    @DisplayName("API REST: Debe consultar recetas, escalar porciones y consultar sugerencias de cocina")
    void shouldInteractViaRestEndpoints() throws Exception {
        ProductResponse fruta = catalogService.createProduct(CreateProductRequest.builder()
            .name("Frutillas Frescas")
            .sku("FRU-01")
            .barCode("7801119998887")
            .unitOfMeasure(UnitOfMeasure.KILOGRAM)
            .build());

        RecipeResponse smoothie = recipeService.createRecipe(CreateRecipeRequest.builder()
            .title("Smoothie de Frutilla")
            .instructions("Licuar frutillas con hielo y agua")
            .servings(2)
            .ingredients(List.of(
                RecipeIngredientRequest.builder()
                    .productId(fruta.getId())
                    .quantity(0.3)
                    .build()
            ))
            .build());

        // 1. Obtener receta por ID
        mockMvc.perform(get("/api/recipes/" + smoothie.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Smoothie de Frutilla"))
            .andExpect(jsonPath("$.servings").value(2));

        // 2. Escalar receta a 4 porciones
        mockMvc.perform(get("/api/recipes/" + smoothie.getId() + "/scale?portions=4"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetServings").value(4))
            .andExpect(jsonPath("$.scalingFactor").value(2.0))
            .andExpect(jsonPath("$.ingredients[0].scaledQuantity").value(0.6));

        // 3. Consultar sugerencias de "¿Qué puedo cocinar hoy?"
        mockMvc.perform(get("/api/recipes/what-can-i-cook"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].recipeTitle").value("Smoothie de Frutilla"));
    }
}
