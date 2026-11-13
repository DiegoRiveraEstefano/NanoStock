package cl.diego.NanoStock.ui;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import cl.diego.NanoStock.recipes.dto.CookRecipeResponse;
import cl.diego.NanoStock.recipes.dto.CreateRecipeRequest;
import cl.diego.NanoStock.recipes.dto.RecipeIngredientRequest;
import cl.diego.NanoStock.recipes.dto.RecipeMatchResponse;
import cl.diego.NanoStock.recipes.dto.RecipeResponse;
import cl.diego.NanoStock.recipes.dto.UpdateRecipeRequest;
import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import cl.diego.NanoStock.recipes.service.RecipeMatchingService;
import cl.diego.NanoStock.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeViewController {

    private final RecipeService recipeService;
    private final RecipeMatchingService matchingService;
    private final ProductCatalogService catalogService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String listRecipes(
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {
        model.addAttribute("recipes", recipeService.getAllRecipes());
        if (flashMessage != null) {
            model.addAttribute("flashMessage", flashMessage);
            model.addAttribute("flashType", flashType);
        } else if (!model.containsAttribute("flashMessage")) {
            model.addAttribute("flashMessage", null);
            model.addAttribute("flashType", flashType);
        }
        return "pages/recipes/index";
    }

    @GetMapping("/new")
    public String newRecipeForm(Model model) {
        CreateRecipeRequest form = CreateRecipeRequest.builder()
            .servings(4)
            .portionType(PortionType.SCALABLE)
            .difficulty(RecipeDifficulty.MEDIUM)
            .category(RecipeCategory.LUNCH)
            .prepTimeMinutes(30)
            .build();
        populateFormModel(model, form, null, null);
        return "pages/recipes/recipe_form";
    }

    @PostMapping
    public String createRecipe(
            @Valid @ModelAttribute("form") CreateRecipeRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("Revisa los campos obligatorios de la receta.");
            populateFormModel(model, form, null, firstError);
            return "pages/recipes/recipe_form";
        }

        try {
            RecipeResponse created = recipeService.createRecipe(form);
            redirectAttributes.addFlashAttribute("flashMessage",
                "Receta '" + created.getTitle() + "' creada correctamente.");
            return "redirect:/recipes/" + created.getId();
        } catch (Exception ex) {
            populateFormModel(model, form, null, ex.getMessage());
            return "pages/recipes/recipe_form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editRecipeForm(@PathVariable Long id, Model model) {
        RecipeResponse recipe = recipeService.getRecipeById(id)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));

        List<RecipeIngredientRequest> ingredientRequests = recipe.getIngredients() == null
            ? List.of()
            : recipe.getIngredients().stream()
                .map(ing -> RecipeIngredientRequest.builder()
                    .productId(ing.getProductId())
                    .quantity(ing.getQuantity())
                    .unitOfMeasure(ing.getUnitOfMeasure())
                    .isOptional(ing.isOptional())
                    .notes(ing.getNotes())
                    .build())
                .toList();

        CreateRecipeRequest form = CreateRecipeRequest.builder()
            .title(recipe.getTitle())
            .description(recipe.getDescription())
            .instructions(recipe.getInstructions())
            .servings(recipe.getServings())
            .portionType(recipe.getPortionType())
            .prepTimeMinutes(recipe.getPrepTimeMinutes())
            .difficulty(recipe.getDifficulty())
            .category(recipe.getCategory())
            .notes(recipe.getNotes())
            .ingredients(ingredientRequests)
            .build();

        populateFormModel(model, form, id, null);
        return "pages/recipes/recipe_form";
    }

    @PostMapping("/{id}")
    public String updateRecipe(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") CreateRecipeRequest form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("Revisa los campos obligatorios de la receta.");
            populateFormModel(model, form, id, firstError);
            return "pages/recipes/recipe_form";
        }

        try {
            UpdateRecipeRequest updateRequest = UpdateRecipeRequest.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .instructions(form.getInstructions())
                .servings(form.getServings())
                .portionType(form.getPortionType())
                .prepTimeMinutes(form.getPrepTimeMinutes())
                .difficulty(form.getDifficulty())
                .category(form.getCategory())
                .notes(form.getNotes())
                .ingredients(form.getIngredients())
                .build();
            RecipeResponse updated = recipeService.updateRecipe(id, updateRequest);
            redirectAttributes.addFlashAttribute("flashMessage",
                "Receta '" + updated.getTitle() + "' actualizada correctamente.");
            return "redirect:/recipes/" + id;
        } catch (Exception ex) {
            populateFormModel(model, form, id, ex.getMessage());
            return "pages/recipes/recipe_form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteRecipe(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            recipeService.deleteRecipe(id);
            redirectAttributes.addFlashAttribute("flashMessage", "Receta eliminada del recetario.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al eliminar receta: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/recipes";
    }

    @GetMapping("/{id}")
    public String recipeDetail(
            @PathVariable Long id,
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) throws JsonProcessingException {

        RecipeResponse recipe = recipeService.getRecipeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));

        model.addAttribute("recipe", recipe);
        model.addAttribute("ingredientsJson", objectMapper.writeValueAsString(recipe.getIngredients()));
        if (flashMessage != null) {
            model.addAttribute("flashMessage", flashMessage);
            model.addAttribute("flashType", flashType);
        } else if (!model.containsAttribute("flashMessage")) {
            model.addAttribute("flashMessage", null);
            model.addAttribute("flashType", flashType);
        }

        return "pages/recipes/recipe_detail";
    }

    @GetMapping("/what-to-cook")
    public String whatToCook(
            @RequestParam(required = false, defaultValue = "4") Integer servings,
            @RequestParam(required = false) RecipeCategory category,
            Model model) {

        List<RecipeMatchResponse> matches = matchingService.findMatchableRecipes(servings, category);

        model.addAttribute("matches", matches);
        model.addAttribute("desiredServings", servings);
        model.addAttribute("selectedCategory", category);

        return "pages/recipes/what_to_cook";
    }

    @PostMapping("/{id}/cook")
    public String cookRecipe(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "4") Integer targetPortions,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (principal != null) ? principal.getName() : "Familia";
            CookRecipeResponse response = recipeService.cookRecipe(id, targetPortions, username);

            int count = (response.getConsumedIngredients() != null) ? response.getConsumedIngredients().size() : 0;
            redirectAttributes.addFlashAttribute("flashMessage", "¡Buen provecho! " + response.getMessage() + " (Se descontaron " + count + " ingredientes según FEFO)");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al cocinar receta: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/recipes/" + id;
    }

    @PostMapping("/{id}/add-missing-to-cart")
    public String addMissingIngredients(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "4") Integer targetPortions,
            RedirectAttributes redirectAttributes) {
        try {
            int addedCount = recipeService.addMissingIngredientsToShoppingList(id, targetPortions);
            redirectAttributes.addFlashAttribute("flashMessage", "Se agregaron " + addedCount + " ingredientes faltantes a tu lista de compras activa.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("flashMessage", "Error al añadir a compras: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "is-danger");
        }
        return "redirect:/purchases/shopping-list";
    }

    private void populateFormModel(Model model, CreateRecipeRequest form, Long recipeId, String errorMessage) {
        model.addAttribute("form", form);
        model.addAttribute("recipeId", recipeId);
        model.addAttribute("products", catalogService.getAllProducts());
        model.addAttribute("unitsOfMeasure", UnitOfMeasure.values());
        model.addAttribute("categories", RecipeCategory.values());
        model.addAttribute("difficulties", RecipeDifficulty.values());
        model.addAttribute("portionTypes", PortionType.values());
        model.addAttribute("errorMessage", errorMessage);
    }
}
