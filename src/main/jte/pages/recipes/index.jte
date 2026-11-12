@import java.util.List
@import cl.diego.NanoStock.recipes.dto.RecipeResponse
@import cl.diego.NanoStock.recipes.model.RecipeCategory

@param List<RecipeResponse> recipes
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Recetario Familiar",
    activeSection = "recipes",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Recetario Familiar</h1>
                    <p class="subtitle is-6 has-text-grey">Planificación culinaria vinculada a existencias y caducidades FEFO.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <a href="/recipes/what-to-cook" class="button is-warning">
                        <span>🍳</span> <span class="ml-1">¿Qué Cocinar Hoy?</span>
                    </a>
                    <a href="/recipes/new" class="button is-primary">
                        <span>➕</span> <span class="ml-1">Nueva Receta</span>
                    </a>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "✅")
        @endif

        @if(recipes.isEmpty())
            <div class="box has-text-centered p-5">
                <span class="is-size-1">📖</span>
                <h2 class="title is-5 mt-2">No hay recetas registradas</h2>
                <p class="has-text-grey">Comienza añadiendo los platos habituales de la familia para vincularlos con tu despensa.</p>
                <a href="/recipes/new" class="button is-primary is-light mt-3">Crear Primera Receta</a>
            </div>
        @else
            <div class="columns is-multiline">
                @for(RecipeResponse r : recipes)
                    <div class="column is-6-tablet is-4-desktop">
                        <div class="card is-flex is-flex-direction-column" style="height: 100%; border-radius: 8px;">
                            <header class="card-header has-background-light">
                                <p class="card-header-title mb-0">
                                    ${r.getTitle()}
                                </p>
                                <span class="tag is-info is-light m-2 is-rounded">
                                    ${r.getCategory() != null ? r.getCategory().name() : "LUNCH"}
                                </span>
                            </header>

                            <div class="card-content is-flex-grow-1">
                                <div class="is-flex is-align-items-center gap-2 mb-3">
                                    <span class="tag is-light">⏱️ ${r.getPrepTimeMinutes() != null ? r.getPrepTimeMinutes() : 30} min</span>
                                    <span class="tag is-light">👥 ${r.getServings() != null ? r.getServings() : 4} raciones</span>
                                    <span class="tag is-light is-rounded">${r.getDifficulty() != null ? r.getDifficulty().name() : "MEDIUM"}</span>
                                </div>

                                <p class="is-size-6 has-text-grey mb-3">
                                    ${r.getDescription() != null && !r.getDescription().isBlank() ? r.getDescription() : "Sin descripción."}
                                </p>

                                <div class="is-size-7 has-text-grey-dark">
                                    <strong>Ingredientes:</strong> ${r.getIngredients() != null ? r.getIngredients().size() : 0} artículos
                                </div>
                            </div>

                            <footer class="card-footer">
                                <a href="/recipes/${r.getId()}" class="card-footer-item has-text-primary has-text-weight-semibold">Ver Receta →</a>
                                <a href="/recipes/${r.getId()}/edit" class="card-footer-item has-text-grey">Editar</a>
                            </footer>
                        </div>
                    </div>
                @endfor
            </div>
        @endif
    `
)
