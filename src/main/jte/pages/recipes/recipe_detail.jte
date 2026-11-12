@import cl.diego.NanoStock.recipes.dto.RecipeResponse
@import cl.diego.NanoStock.recipes.dto.ScaledRecipeResponse

@param RecipeResponse recipe
@param String ingredientsJson
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = recipe.getTitle(),
    activeSection = "recipes",
    content = @`
        <nav class="breadcrumb has-succeeds-separator is-small mb-4" aria-label="breadcrumbs">
            <ul>
                <li><a href="/recipes">Recetario</a></li>
                <li class="is-active"><a href="#" aria-current="page">${recipe.getTitle()}</a></li>
            </ul>
        </nav>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "🍲")
        @endif

        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-3 mb-1">${recipe.getTitle()}</h1>
                    <div class="tags are-medium mt-1">
                        <span class="tag is-info is-light">📂 ${recipe.getCategory() != null ? recipe.getCategory().name() : "LUNCH"}</span>
                        <span class="tag is-light">⏱️ ${recipe.getPrepTimeMinutes() != null ? recipe.getPrepTimeMinutes() : 30} min</span>
                        <span class="tag is-light">⭐ Dificultad: ${recipe.getDifficulty() != null ? recipe.getDifficulty().name() : "MEDIUM"}</span>
                    </div>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <form action="/recipes/${recipe.getId()}/add-missing-to-cart" method="post" class="is-inline">
                        <input type="hidden" name="targetPortions" value="${recipe.getServings()}">
                        <button type="submit" class="button is-info is-light" title="Enviar faltantes al carrito">
                            <span>🛒</span> <span class="ml-1">Añadir Faltantes a Compras</span>
                        </button>
                    </form>

                    <form action="/recipes/${recipe.getId()}/cook" method="post" class="is-inline">
                        <input type="hidden" name="targetPortions" value="${recipe.getServings()}">
                        <button type="submit" class="button is-primary" onclick="return confirm('¿Cocinar esta receta ahora? Se descontarán todos los ingredientes del inventario según FEFO.')">
                            <span>🍳</span> <span class="ml-1">Cocinar Receta</span>
                        </button>
                    </form>
                </div>
            </div>
        </div>

        <div class="columns">
            <!-- Ingredientes y Escalador Reactivo -->
            <div class="column is-5">
                <h2 class="title is-5 mb-3">Ingredientes Proporcionales</h2>
                <nano-recipe-scaler 
                    base-servings="${recipe.getServings() != null ? recipe.getServings() : 4}" 
                    ingredients-data="${ingredientsJson}">
                </nano-recipe-scaler>
            </div>

            <!-- Instrucciones de Preparación -->
            <div class="column is-7">
                <div class="box">
                    <h2 class="title is-5 mb-3">Instrucciones de Preparación</h2>
                    <div class="content is-size-6" style="white-space: pre-line; line-height: 1.7;">
                        ${recipe.getInstructions()}
                    </div>

                    @if(recipe.getNotes() != null && !recipe.getNotes().isBlank())
                        <div class="notification is-light mt-4">
                            <h3 class="has-text-weight-bold is-size-6 mb-1">💡 Notas Culinarias del Chef:</h3>
                            <p class="is-size-7">${recipe.getNotes()}</p>
                        </div>
                    @endif
                </div>
            </div>
        </div>
    `,
    scripts = @`
        <script type="module" src="/js/components/nano-recipe-scaler.js"></script>
    `
)
