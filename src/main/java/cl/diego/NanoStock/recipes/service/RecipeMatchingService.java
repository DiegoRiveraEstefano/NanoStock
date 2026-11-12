package cl.diego.NanoStock.recipes.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.recipes.dto.RecipeMatchResponse;
import cl.diego.NanoStock.recipes.dto.ScaledRecipeResponse;
import cl.diego.NanoStock.recipes.model.Recipe;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeMatchingService {

    private final RecipeRepository recipeRepository;
    private final RecipeService recipeService;
    private final StockBatchRepository stockBatchRepository;

    @Transactional(readOnly = true)
    public List<RecipeMatchResponse> findMatchableRecipes(Integer desiredServings, RecipeCategory category) {
        List<Recipe> recipes = (category != null)
            ? recipeRepository.findByCategory(category)
            : recipeRepository.findAll();

        // Obtener productos con lotes que vencen en los próximos 7 días para priorización inteligente
        LocalDate horizon = LocalDate.now().plusDays(7);
        List<StockBatch> expiringBatches = stockBatchRepository
            .findByQuantityGreaterThanAndExpirationDateLessThanEqualOrderByExpirationDateAsc(0.0, horizon);

        Set<Long> expiringProductIds = expiringBatches.stream()
            .map(b -> b.getProduct().getId())
            .collect(Collectors.toSet());

        List<RecipeMatchResponse> matches = new ArrayList<>();

        for (Recipe recipe : recipes) {
            int servings = (desiredServings != null && desiredServings > 0)
                ? desiredServings
                : recipe.getServings();

            ScaledRecipeResponse scaled = recipeService.scaleRecipe(recipe.getId(), servings);

            int totalMandatory = 0;
            int sufficientMandatory = 0;
            int expiringUsed = 0;
            List<RecipeMatchResponse.MissingIngredientDto> missingList = new ArrayList<>();

            for (ScaledRecipeResponse.ScaledIngredientDto ing : scaled.getIngredients()) {
                if (!ing.isOptional()) {
                    totalMandatory++;
                    if (ing.isSufficient()) {
                        sufficientMandatory++;
                    } else {
                        double missing = BigDecimal.valueOf(ing.getScaledQuantity() - ing.getAvailableStock())
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();

                        missingList.add(RecipeMatchResponse.MissingIngredientDto.builder()
                            .productId(ing.getProductId())
                            .productName(ing.getProductName())
                            .requiredQuantity(ing.getScaledQuantity())
                            .availableStock(ing.getAvailableStock())
                            .missingQuantity(missing)
                            .unitOfMeasure(ing.getUnitOfMeasure())
                            .isOptional(false)
                            .build());
                    }
                } else if (!ing.isSufficient()) {
                    double missing = BigDecimal.valueOf(ing.getScaledQuantity() - ing.getAvailableStock())
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

                    missingList.add(RecipeMatchResponse.MissingIngredientDto.builder()
                        .productId(ing.getProductId())
                        .productName(ing.getProductName())
                        .requiredQuantity(ing.getScaledQuantity())
                        .availableStock(ing.getAvailableStock())
                        .missingQuantity(missing)
                        .unitOfMeasure(ing.getUnitOfMeasure())
                        .isOptional(true)
                        .build());
                }

                // Si tenemos existencias y además ese ingrediente está pronto a vencer, suma prioridad
                if (ing.isSufficient() && expiringProductIds.contains(ing.getProductId())) {
                    expiringUsed++;
                }
            }

            double viability = totalMandatory > 0
                ? BigDecimal.valueOf(((double) sufficientMandatory / (double) totalMandatory) * 100.0)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue()
                : 100.0;

            boolean canCookNow = (totalMandatory == sufficientMandatory);

            matches.add(RecipeMatchResponse.builder()
                .recipeId(recipe.getId())
                .recipeTitle(recipe.getTitle())
                .portionType(recipe.getPortionType())
                .servings(servings)
                .prepTimeMinutes(recipe.getPrepTimeMinutes())
                .difficulty(recipe.getDifficulty())
                .category(recipe.getCategory())
                .notes(recipe.getNotes())
                .viabilityPercentage(viability)
                .canCookNow(canCookNow)
                .expiringIngredientsCount(expiringUsed)
                .missingIngredients(missingList)
                .build());
        }

        // Orden de recomendación:
        // 1. Las que se pueden cocinar inmediatamente primero (canCookNow desc)
        // 2. Mayor cantidad de ingredientes prontos a vencer (expiringIngredientsCount desc)
        // 3. Mayor porcentaje de viabilidad (viabilityPercentage desc)
        // 4. Alfabético por título
        matches.sort(Comparator
            .comparing(RecipeMatchResponse::isCanCookNow).reversed()
            .thenComparing(RecipeMatchResponse::getExpiringIngredientsCount, Comparator.reverseOrder())
            .thenComparing(RecipeMatchResponse::getViabilityPercentage, Comparator.reverseOrder())
            .thenComparing(RecipeMatchResponse::getRecipeTitle));

        return matches;
    }
}
