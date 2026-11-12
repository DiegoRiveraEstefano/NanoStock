package cl.diego.NanoStock.ui;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.recipes.dto.CreateRecipeRequest;
import cl.diego.NanoStock.recipes.dto.RecipeIngredientRequest;
import cl.diego.NanoStock.recipes.dto.RecipeResponse;
import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import cl.diego.NanoStock.recipes.service.RecipeService;

@SpringBootTest
@ActiveProfiles("test")
class RecipeViewControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ProductCatalogService catalogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /recipes debe desplegar el recetario familiar")
    void shouldRenderRecipesList() throws Exception {
        mockMvc.perform(get("/recipes"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/recipes/index"))
                .andExpect(model().attributeExists("recipes"))
                .andExpect(content().string(containsString("Recetario Familiar")));
    }

    @Test
    @DisplayName("GET /recipes/{id} debe mostrar el detalle con el escalador nano-recipe-scaler")
    void shouldRenderRecipeDetail() throws Exception {
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Pasta Spaghetti " + System.currentTimeMillis())
                .barCode("78011" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.GRAM)
                .minStockThreshold(200.0)
                .targetStockQuantity(500.0)
                .isPerishable(false)
                .build());

        RecipeResponse recipe = recipeService.createRecipe(CreateRecipeRequest.builder()
                .title("Spaghetti al Pesto " + System.currentTimeMillis())
                .description("Clásico plato italiano")
                .instructions("1. Hervir agua con sal.\n2. Cocinar pasta 8 minutos.\n3. Mezclar con salsa pesto.")
                .servings(4)
                .portionType(PortionType.SCALABLE)
                .prepTimeMinutes(20)
                .difficulty(RecipeDifficulty.EASY)
                .category(RecipeCategory.LUNCH)
                .notes("Usar albahaca fresca")
                .ingredients(List.of(
                        RecipeIngredientRequest.builder()
                                .productId(prod.getId())
                                .quantity(400.0)
                                .unitOfMeasure(UnitOfMeasure.GRAM)
                                .isOptional(false)
                                .build()
                ))
                .build());

        mockMvc.perform(get("/recipes/" + recipe.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/recipes/recipe_detail"))
                .andExpect(model().attributeExists("recipe", "ingredientsJson"))
                .andExpect(content().string(containsString("Spaghetti al Pesto")))
                .andExpect(content().string(containsString("nano-recipe-scaler")));
    }

    @Test
    @DisplayName("GET /recipes/what-to-cook debe ejecutar el asistente culinario de matching")
    void shouldRenderWhatToCook() throws Exception {
        mockMvc.perform(get("/recipes/what-to-cook")
                .param("servings", "4"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/recipes/what_to_cook"))
                .andExpect(model().attributeExists("matches", "desiredServings"))
                .andExpect(content().string(containsString("¿Qué puedo cocinar hoy?")));
    }

    @Test
    @DisplayName("POST /recipes/{id}/add-missing-to-cart debe traspasar faltantes a compras")
    void shouldAddMissingToCart() throws Exception {
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Carne Molida " + System.currentTimeMillis())
                .barCode("78022" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.GRAM)
                .minStockThreshold(300.0)
                .targetStockQuantity(1000.0)
                .isPerishable(true)
                .build());

        RecipeResponse recipe = recipeService.createRecipe(CreateRecipeRequest.builder()
                .title("Hamburguesa Casera " + System.currentTimeMillis())
                .instructions("Armar discos de carne y asar a la plancha.")
                .servings(2)
                .ingredients(List.of(
                        RecipeIngredientRequest.builder()
                                .productId(prod.getId())
                                .quantity(500.0)
                                .unitOfMeasure(UnitOfMeasure.GRAM)
                                .build()
                ))
                .build());

        mockMvc.perform(post("/recipes/" + recipe.getId() + "/add-missing-to-cart")
                .param("targetPortions", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/purchases/shopping-list"));
    }
}
