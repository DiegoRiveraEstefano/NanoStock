package cl.diego.NanoStock.recipes.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.inventory.dto.ConsumeStockRequest;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.inventory.service.InventoryService;
import cl.diego.NanoStock.purchases.dto.AddShoppingItemRequest;
import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.service.ShoppingListService;
import cl.diego.NanoStock.recipes.dto.CookRecipeResponse;
import cl.diego.NanoStock.recipes.dto.CreateRecipeRequest;
import cl.diego.NanoStock.recipes.dto.RecipeIngredientRequest;
import cl.diego.NanoStock.recipes.dto.RecipeIngredientResponse;
import cl.diego.NanoStock.recipes.dto.RecipeResponse;
import cl.diego.NanoStock.recipes.dto.ScaledRecipeResponse;
import cl.diego.NanoStock.recipes.dto.UpdateRecipeRequest;
import cl.diego.NanoStock.recipes.event.RecipeCookedEvent;
import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.Recipe;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import cl.diego.NanoStock.recipes.model.RecipeIngredient;
import cl.diego.NanoStock.recipes.repository.RecipeIngredientRepository;
import cl.diego.NanoStock.recipes.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final StockBatchRepository stockBatchRepository;
    private final InventoryService inventoryService;
    private final ShoppingListService shoppingListService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RecipeResponse createRecipe(CreateRecipeRequest request) {
        int servings = request.getServings() != null && request.getServings() > 0
            ? request.getServings()
            : (request.getPortionType() == PortionType.FIXED_SINGLE ? 1 : 4);

        Recipe recipe = Recipe.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .instructions(request.getInstructions())
            .servings(servings)
            .portionType(request.getPortionType() != null ? request.getPortionType() : PortionType.SCALABLE)
            .prepTimeMinutes(request.getPrepTimeMinutes())
            .difficulty(request.getDifficulty() != null ? request.getDifficulty() : RecipeDifficulty.MEDIUM)
            .category(request.getCategory() != null ? request.getCategory() : RecipeCategory.LUNCH)
            .notes(request.getNotes())
            .build();

        if (request.getIngredients() != null) {
            for (RecipeIngredientRequest ingReq : request.getIngredients()) {
                Product product = productRepository.findById(ingReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + ingReq.getProductId()));

                UnitOfMeasure uom = ingReq.getUnitOfMeasure() != null ? ingReq.getUnitOfMeasure() : product.getUnitOfMeasure();

                RecipeIngredient ingredient = RecipeIngredient.builder()
                    .product(product)
                    .quantity(ingReq.getQuantity())
                    .unitOfMeasure(uom)
                    .isOptional(ingReq.isOptional())
                    .notes(ingReq.getNotes())
                    .build();

                recipe.addIngredient(ingredient);
            }
        }

        Recipe saved = recipeRepository.save(recipe);
        log.info("Receta creada exitosamente: '{}' (ID: {})", saved.getTitle(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public RecipeResponse updateRecipe(Long id, UpdateRecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con ID: " + id));

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setInstructions(request.getInstructions());
        if (request.getServings() != null && request.getServings() > 0) {
            recipe.setServings(request.getServings());
        }
        if (request.getPortionType() != null) {
            recipe.setPortionType(request.getPortionType());
        }
        recipe.setPrepTimeMinutes(request.getPrepTimeMinutes());
        if (request.getDifficulty() != null) {
            recipe.setDifficulty(request.getDifficulty());
        }
        if (request.getCategory() != null) {
            recipe.setCategory(request.getCategory());
        }
        recipe.setNotes(request.getNotes());

        if (request.getIngredients() != null) {
            recipe.getIngredients().clear();
            for (RecipeIngredientRequest ingReq : request.getIngredients()) {
                Product product = productRepository.findById(ingReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + ingReq.getProductId()));

                UnitOfMeasure uom = ingReq.getUnitOfMeasure() != null ? ingReq.getUnitOfMeasure() : product.getUnitOfMeasure();

                RecipeIngredient ingredient = RecipeIngredient.builder()
                    .product(product)
                    .quantity(ingReq.getQuantity())
                    .unitOfMeasure(uom)
                    .isOptional(ingReq.isOptional())
                    .notes(ingReq.getNotes())
                    .build();

                recipe.addIngredient(ingredient);
            }
        }

        Recipe updated = recipeRepository.save(recipe);
        log.info("Receta actualizada exitosamente: '{}' (ID: {})", updated.getTitle(), updated.getId());
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Optional<RecipeResponse> getRecipeById(Long id) {
        return recipeRepository.findById(id).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new IllegalArgumentException("Receta no encontrada con ID: " + id);
        }
        recipeRepository.deleteById(id);
        log.info("Receta eliminada (ID: {})", id);
    }

    @Transactional(readOnly = true)
    public ScaledRecipeResponse scaleRecipe(Long recipeId, int targetServings) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con ID: " + recipeId));

        int target = targetServings > 0 ? targetServings : recipe.getServings();
        double factor = (double) target / (double) recipe.getServings();

        boolean canCook = true;
        List<ScaledRecipeResponse.ScaledIngredientDto> scaledList = new ArrayList<>();

        for (RecipeIngredient ing : recipe.getIngredients()) {
            double baseQty = ing.getQuantity();
            double scaledQty = BigDecimal.valueOf(baseQty * factor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

            Double available = stockBatchRepository.sumQuantityByProductId(ing.getProduct().getId());
            boolean sufficient = available >= scaledQty;

            if (!ing.isOptional() && !sufficient) {
                canCook = false;
            }

            scaledList.add(ScaledRecipeResponse.ScaledIngredientDto.builder()
                .productId(ing.getProduct().getId())
                .productName(ing.getProduct().getName())
                .baseQuantity(baseQty)
                .scaledQuantity(scaledQty)
                .availableStock(available)
                .isSufficient(sufficient)
                .unitOfMeasure(ing.getUnitOfMeasure())
                .isOptional(ing.isOptional())
                .notes(ing.getNotes())
                .build());
        }

        return ScaledRecipeResponse.builder()
            .recipeId(recipe.getId())
            .recipeTitle(recipe.getTitle())
            .portionType(recipe.getPortionType())
            .baseServings(recipe.getServings())
            .targetServings(target)
            .scalingFactor(factor)
            .canCookTargetServings(canCook)
            .ingredients(scaledList)
            .build();
    }

    @Transactional
    public CookRecipeResponse cookRecipe(Long recipeId, Integer targetPortions, String username) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con ID: " + recipeId));

        int portions = (targetPortions != null && targetPortions > 0) ? targetPortions : recipe.getServings();
        ScaledRecipeResponse scaled = scaleRecipe(recipeId, portions);

        if (!scaled.isCanCookTargetServings()) {
            throw new IllegalArgumentException(String.format(
                "No hay existencias suficientes de ingredientes obligatorios en el inventario para preparar %d raciones de '%s'",
                portions, recipe.getTitle()));
        }

        List<CookRecipeResponse.ConsumedIngredientDto> consumedList = new ArrayList<>();
        List<RecipeCookedEvent.CookedIngredientDetail> eventDetails = new ArrayList<>();

        for (ScaledRecipeResponse.ScaledIngredientDto ing : scaled.getIngredients()) {
            if (ing.getScaledQuantity() > 0) {
                // Deducción atómica del stock mediante FEFO
                inventoryService.consumeStock(ConsumeStockRequest.builder()
                    .productId(ing.getProductId())
                    .quantity(ing.getScaledQuantity())
                    .notes(String.format("Preparación de receta: %s (%d porciones)", recipe.getTitle(), portions))
                    .build(), username);

                consumedList.add(CookRecipeResponse.ConsumedIngredientDto.builder()
                    .productId(ing.getProductId())
                    .productName(ing.getProductName())
                    .quantityConsumed(ing.getScaledQuantity())
                    .unitOfMeasure(ing.getUnitOfMeasure())
                    .build());

                eventDetails.add(new RecipeCookedEvent.CookedIngredientDetail(
                    ing.getProductId(),
                    ing.getProductName(),
                    ing.getScaledQuantity(),
                    ing.getUnitOfMeasure().name()
                ));
            }
        }

        eventPublisher.publishEvent(new RecipeCookedEvent(
            recipe.getId(),
            recipe.getTitle(),
            portions,
            eventDetails,
            username != null ? username : "sistema",
            LocalDateTime.now()
        ));

        log.info("Receta '{}' cocinada exitosamente para {} porciones por {}", recipe.getTitle(), portions, username);

        return CookRecipeResponse.builder()
            .recipeId(recipe.getId())
            .recipeTitle(recipe.getTitle())
            .portionsCooked(portions)
            .message(String.format("¡Buen provecho! Se descontaron %d ingredientes de inventario para %d porciones.",
                consumedList.size(), portions))
            .consumedIngredients(consumedList)
            .build();
    }

    @Transactional
    public int addMissingIngredientsToShoppingList(Long recipeId, Integer targetPortions) {
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con ID: " + recipeId));

        int portions = (targetPortions != null && targetPortions > 0) ? targetPortions : recipe.getServings();
        ScaledRecipeResponse scaled = scaleRecipe(recipeId, portions);

        ShoppingList activeList = shoppingListService.getOrCreateActiveList();
        int addedCount = 0;

        for (ScaledRecipeResponse.ScaledIngredientDto ing : scaled.getIngredients()) {
            if (!ing.isSufficient()) {
                double missing = ing.getScaledQuantity() - ing.getAvailableStock();
                if (missing > 0) {
                    shoppingListService.addItem(activeList.getId(), AddShoppingItemRequest.builder()
                        .productId(ing.getProductId())
                        .quantity(BigDecimal.valueOf(missing).setScale(2, RoundingMode.CEILING).doubleValue())
                        .build());
                    addedCount++;
                }
            }
        }

        log.info("Se añadieron {} ingredientes faltantes de la receta '{}' a la lista de compras activa",
            addedCount, recipe.getTitle());
        return addedCount;
    }

    private RecipeResponse toResponse(Recipe r) {
        List<RecipeIngredientResponse> ingDtos = r.getIngredients() != null
            ? r.getIngredients().stream().map(this::toIngredientResponse).toList()
            : List.of();

        return RecipeResponse.builder()
            .id(r.getId())
            .title(r.getTitle())
            .description(r.getDescription())
            .instructions(r.getInstructions())
            .servings(r.getServings())
            .portionType(r.getPortionType())
            .prepTimeMinutes(r.getPrepTimeMinutes())
            .difficulty(r.getDifficulty())
            .category(r.getCategory())
            .notes(r.getNotes())
            .ingredients(ingDtos)
            .createdAt(r.getCreatedAt())
            .build();
    }

    private RecipeIngredientResponse toIngredientResponse(RecipeIngredient ing) {
        return RecipeIngredientResponse.builder()
            .id(ing.getId())
            .productId(ing.getProduct().getId())
            .productName(ing.getProduct().getName())
            .quantity(ing.getQuantity())
            .unitOfMeasure(ing.getUnitOfMeasure())
            .isOptional(ing.isOptional())
            .notes(ing.getNotes())
            .build();
    }
}
