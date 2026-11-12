package cl.diego.NanoStock.recipes.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.recipes.dto.CookRecipeRequest;
import cl.diego.NanoStock.recipes.dto.CookRecipeResponse;
import cl.diego.NanoStock.recipes.dto.CreateRecipeRequest;
import cl.diego.NanoStock.recipes.dto.RecipeMatchResponse;
import cl.diego.NanoStock.recipes.dto.RecipeResponse;
import cl.diego.NanoStock.recipes.dto.ScaledRecipeResponse;
import cl.diego.NanoStock.recipes.dto.UpdateRecipeRequest;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.service.RecipeMatchingService;
import cl.diego.NanoStock.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeMatchingService recipeMatchingService;

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody CreateRecipeRequest request) {
        RecipeResponse created = recipeService.createRecipe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecipeRequest request) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/scale")
    public ResponseEntity<ScaledRecipeResponse> scaleRecipe(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") int portions) {
        return ResponseEntity.ok(recipeService.scaleRecipe(id, portions));
    }

    @PostMapping("/{id}/cook")
    public ResponseEntity<CookRecipeResponse> cookRecipe(
            @PathVariable Long id,
            @RequestBody(required = false) CookRecipeRequest request,
            Principal principal) {
        String username = principal != null ? principal.getName() : "usuario";
        Integer portions = request != null ? request.getPortions() : null;
        CookRecipeResponse response = recipeService.cookRecipe(id, portions, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/add-missing-to-cart")
    public ResponseEntity<Map<String, Object>> addMissingToCart(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "0") int portions) {
        int added = recipeService.addMissingIngredientsToShoppingList(id, portions > 0 ? portions : null);
        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "message", String.format("Se agregaron %d ingredientes faltantes a la lista de compras activa", added),
            "addedItemsCount", added
        ));
    }

    @GetMapping("/what-can-i-cook")
    public ResponseEntity<List<RecipeMatchResponse>> whatCanICook(
            @RequestParam(required = false) Integer servings,
            @RequestParam(required = false) RecipeCategory category) {
        return ResponseEntity.ok(recipeMatchingService.findMatchableRecipes(servings, category));
    }
}
