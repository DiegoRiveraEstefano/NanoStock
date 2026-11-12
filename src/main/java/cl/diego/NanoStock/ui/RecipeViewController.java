package cl.diego.NanoStock.ui;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.recipes.dto.CookRecipeResponse;
import cl.diego.NanoStock.recipes.dto.RecipeMatchResponse;
import cl.diego.NanoStock.recipes.dto.RecipeResponse;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.service.RecipeMatchingService;
import cl.diego.NanoStock.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeViewController {

    private final RecipeService recipeService;
    private final RecipeMatchingService matchingService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String listRecipes(
            @RequestParam(required = false) String flashMessage,
            @RequestParam(required = false, defaultValue = "is-success") String flashType,
            Model model) {
        model.addAttribute("recipes", recipeService.getAllRecipes());
        model.addAttribute("flashMessage", flashMessage);
        model.addAttribute("flashType", flashType);
        return "pages/recipes/index";
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
        model.addAttribute("flashMessage", flashMessage);
        model.addAttribute("flashType", flashType);

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
}
