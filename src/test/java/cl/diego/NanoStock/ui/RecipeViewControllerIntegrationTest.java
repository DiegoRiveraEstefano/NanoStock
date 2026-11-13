package cl.diego.NanoStock.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
@WithMockUser(username = "diego", roles = {"MEMBER"})
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
    @DisplayName("GET /recipes/new debe mostrar formulario de creación de receta")
    void shouldRenderNewRecipeForm() throws Exception {
        mockMvc.perform(get("/recipes/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/recipes/recipe_form"))
                .andExpect(model().attributeExists("form", "products", "unitsOfMeasure", "difficulties", "categories", "portionTypes"))
                .andExpect(content().string(containsString("Registrar Nueva Receta")));
    }

    @Test
    @DisplayName("POST /recipes debe crear una receta con ingredientes y redirigir al detalle")
    void shouldCreateRecipeFromUi() throws Exception {
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Huevo Orgánico " + System.currentTimeMillis())
                .barCode("78033" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .minStockThreshold(6.0)
                .targetStockQuantity(12.0)
                .isPerishable(true)
                .build());

        String title = "Tortilla Española UI " + System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/recipes")
                .param("title", title)
                .param("description", "Receta tradicional")
                .param("instructions", "1. Batir huevos.\n2. Cocinar a fuego lento.")
                .param("servings", "4")
                .param("portionType", "SCALABLE")
                .param("prepTimeMinutes", "25")
                .param("difficulty", "EASY")
                .param("category", "DINNER")
                .param("notes", "Servir tibia")
                .param("ingredients[0].productId", prod.getId().toString())
                .param("ingredients[0].quantity", "4.0")
                .param("ingredients[0].unitOfMeasure", "UNIT")
                .param("ingredients[0].optional", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/recipes/*"))
                .andExpect(flash().attribute("flashMessage", containsString(title)))
                .andReturn();

        String redirected = result.getResponse().getRedirectedUrl();
        assertThat(redirected).isNotNull();
        Long createdId = Long.parseLong(redirected.substring("/recipes/".length()));

        RecipeResponse saved = recipeService.getRecipeById(createdId).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo(title);
        assertThat(saved.getIngredients()).hasSize(1);
        assertThat(saved.getIngredients().get(0).getProductId()).isEqualTo(prod.getId());
    }

    @Test
    @DisplayName("GET /recipes/{id}/edit y POST /recipes/{id} deben editar una receta existente")
    void shouldEditAndUpdateRecipeFromUi() throws Exception {
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Avena Tradicional " + System.currentTimeMillis())
                .barCode("78044" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.GRAM)
                .minStockThreshold(200.0)
                .targetStockQuantity(500.0)
                .isPerishable(false)
                .build());

        RecipeResponse recipe = recipeService.createRecipe(CreateRecipeRequest.builder()
                .title("Porridge Original " + System.currentTimeMillis())
                .instructions("Cocinar avena con agua.")
                .servings(2)
                .portionType(PortionType.SCALABLE)
                .prepTimeMinutes(10)
                .difficulty(RecipeDifficulty.EASY)
                .category(RecipeCategory.BREAKFAST)
                .ingredients(List.of(
                        RecipeIngredientRequest.builder()
                                .productId(prod.getId())
                                .quantity(100.0)
                                .unitOfMeasure(UnitOfMeasure.GRAM)
                                .isOptional(false)
                                .build()
                ))
                .build());

        mockMvc.perform(get("/recipes/" + recipe.getId() + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/recipes/recipe_form"))
                .andExpect(model().attribute("recipeId", recipe.getId()))
                .andExpect(content().string(containsString("Editar Receta Familiar")));

        String updatedTitle = "Porridge Proteico " + System.currentTimeMillis();
        mockMvc.perform(post("/recipes/" + recipe.getId())
                .param("title", updatedTitle)
                .param("description", "Con canela")
                .param("instructions", "Cocinar 8 min.")
                .param("servings", "3")
                .param("portionType", "SCALABLE")
                .param("prepTimeMinutes", "12")
                .param("difficulty", "MEDIUM")
                .param("category", "BREAKFAST")
                .param("ingredients[0].productId", prod.getId().toString())
                .param("ingredients[0].quantity", "150.0")
                .param("ingredients[0].unitOfMeasure", "GRAM")
                .param("ingredients[0].optional", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes/" + recipe.getId()))
                .andExpect(flash().attribute("flashMessage", containsString(updatedTitle)));

        RecipeResponse reloaded = recipeService.getRecipeById(recipe.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo(updatedTitle);
        assertThat(reloaded.getServings()).isEqualTo(3);
        assertThat(reloaded.getIngredients()).hasSize(1);
        assertThat(reloaded.getIngredients().get(0).getQuantity()).isEqualTo(150.0);
        assertThat(reloaded.getIngredients().get(0).isOptional()).isTrue();
    }

    @Test
    @DisplayName("POST /recipes/{id}/delete debe eliminar la receta y redirigir a /recipes")
    void shouldDeleteRecipeFromUi() throws Exception {
        ProductResponse prod = catalogService.createProduct(CreateProductRequest.builder()
                .name("Té Verde " + System.currentTimeMillis())
                .barCode("78045" + System.currentTimeMillis())
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .minStockThreshold(5.0)
                .targetStockQuantity(20.0)
                .isPerishable(false)
                .build());

        RecipeResponse recipe = recipeService.createRecipe(CreateRecipeRequest.builder()
                .title("Infusión Borrable " + System.currentTimeMillis())
                .instructions("Infusionar en agua caliente.")
                .servings(1)
                .ingredients(List.of(
                        RecipeIngredientRequest.builder()
                                .productId(prod.getId())
                                .quantity(1.0)
                                .unitOfMeasure(UnitOfMeasure.UNIT)
                                .build()
                ))
                .build());

        mockMvc.perform(post("/recipes/" + recipe.getId() + "/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipes"))
                .andExpect(flash().attribute("flashMessage", containsString("eliminada")));

        assertThat(recipeService.getRecipeById(recipe.getId())).isEmpty();
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
