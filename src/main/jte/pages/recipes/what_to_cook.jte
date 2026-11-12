@import java.util.List
@import cl.diego.NanoStock.recipes.dto.RecipeMatchResponse
@import cl.diego.NanoStock.recipes.model.RecipeCategory

@param List<RecipeMatchResponse> matches
@param Integer desiredServings = 4
@param RecipeCategory selectedCategory = null

@template.layouts.app(
    title = "¿Qué Cocinar Hoy?",
    activeSection = "what-to-cook",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">🍳 Asistente Culinario: ¿Qué puedo cocinar hoy?</h1>
                    <p class="subtitle is-6 has-text-grey">
                        Compara tu despensa física en tiempo real y prioriza recetas cuyos ingredientes están próximos a caducar para evitar mermas.
                    </p>
                </div>
            </div>
            <div class="level-right">
                <a href="/recipes" class="button is-light">Ver Todas las Recetas</a>
            </div>
        </div>

        <!-- Filtros de Asistente -->
        <div class="box p-3 mb-5">
            <form action="/recipes/what-to-cook" method="get">
                <div class="field is-grouped is-grouped-multiline">
                    <div class="control">
                        <label class="label is-small">Comensales</label>
                        <input class="input is-small" type="number" name="servings" value="${desiredServings}" min="1" max="15" style="width: 80px;">
                    </div>

                    <div class="control">
                        <label class="label is-small">Categoría</label>
                        <div class="select is-small">
                            <select name="category" onchange="this.form.submit()">
                                <option value="">Todas</option>
                                <option value="BREAKFAST" selected="${selectedCategory != null && selectedCategory.name().equals("BREAKFAST")}">Desayuno</option>
                                <option value="LUNCH" selected="${selectedCategory != null && selectedCategory.name().equals("LUNCH")}">Almuerzo</option>
                                <option value="DINNER" selected="${selectedCategory != null && selectedCategory.name().equals("DINNER")}">Cena</option>
                                <option value="SNACK" selected="${selectedCategory != null && selectedCategory.name().equals("SNACK")}">Merienda / Snack</option>
                                <option value="DESSERT" selected="${selectedCategory != null && selectedCategory.name().equals("DESSERT")}">Postre</option>
                            </select>
                        </div>
                    </div>

                    <div class="control is-align-self-flex-end">
                        <button type="submit" class="button is-small is-primary">Actualizar Matching</button>
                    </div>
                </div>
            </form>
        </div>

        @if(matches.isEmpty())
            <div class="box has-text-centered p-5">
                <span class="is-size-2">🔍</span>
                <h2 class="title is-5 mt-2">No se encontraron recetas para esta combinación</h2>
                <p class="has-text-grey">Prueba cambiando la cantidad de comensales o añade más recetas a tu colección familiar.</p>
            </div>
        @else
            <div class="columns is-multiline">
                @for(RecipeMatchResponse m : matches)
                    <div class="column is-6">
                        <div class="card" style="border-radius: 8px; border-left: 6px solid ${m.isCanCookNow() ? "#00b894" : (m.getViabilityPercentage() >= 50.0 ? "#fdcb6e" : "#ff7675")};">
                            <div class="card-content">
                                <div class="level mb-2">
                                    <div class="level-left">
                                        <h3 class="title is-5 mb-0">
                                            <a href="/recipes/${m.getRecipeId()}" class="has-text-dark">
                                                ${m.getRecipeTitle()}
                                            </a>
                                        </h3>
                                    </div>
                                    <div class="level-right">
                                        @if(m.isCanCookNow())
                                            <span class="tag is-success has-text-weight-bold is-rounded">¡Lista para cocinar! 100%</span>
                                        @else
                                            <span class="tag is-warning is-light has-text-weight-bold is-rounded">Viabilidad: ${String.format("%.0f", m.getViabilityPercentage())}%</span>
                                        @endif
                                    </div>
                                </div>

                                <!-- Bonificación FEFO -->
                                @if(m.getExpiringIngredientsCount() > 0)
                                    <div class="notification is-warning is-light p-2 mb-3 is-size-7">
                                        <span class="mr-1">⚡</span>
                                        <strong>¡Aprovecha los ingredientes!</strong> Usa ${m.getExpiringIngredientsCount()} insumo(s) que están próximos a caducar.
                                    </div>
                                @endif

                                <div class="tags are-small mb-3">
                                    <span class="tag is-light">⏱️ ${m.getPrepTimeMinutes() != null ? m.getPrepTimeMinutes() : 30} min</span>
                                    <span class="tag is-light">👥 ${m.getServings()} raciones</span>
                                    <span class="tag is-light">${m.getDifficulty() != null ? m.getDifficulty().name() : "MEDIUM"}</span>
                                </div>

                                <!-- Faltantes de stock si no está al 100% -->
                                @if(!m.isCanCookNow() && m.getMissingIngredients() != null && !m.getMissingIngredients().isEmpty())
                                    <div class="mb-3">
                                        <h4 class="is-size-7 has-text-weight-bold has-text-grey">Ingredientes Faltantes en Casa:</h4>
                                        <ul class="is-size-7 ml-3">
                                            @for(var miss : m.getMissingIngredients())
                                                <li>
                                                    ❌ <strong>${miss.getProductName()}</strong>: faltan ${miss.getMissingQuantity()} ${miss.getUnitOfMeasure() != null ? miss.getUnitOfMeasure().getSymbol() : "u"}
                                                </li>
                                            @endfor
                                        </ul>
                                    </div>
                                @endif

                                <div class="buttons are-small mt-3">
                                    <a href="/recipes/${m.getRecipeId()}" class="button is-light">Ver Preparación</a>
                                    
                                    @if(m.isCanCookNow())
                                        <form action="/recipes/${m.getRecipeId()}/cook" method="post" class="is-inline">
                                            <input type="hidden" name="targetPortions" value="${desiredServings}">
                                            <button type="submit" class="button is-primary">
                                                <span>🍳</span> <span class="ml-1">Cocinar Ahora</span>
                                            </button>
                                        </form>
                                    @else
                                        <form action="/recipes/${m.getRecipeId()}/add-missing-to-cart" method="post" class="is-inline">
                                            <input type="hidden" name="targetPortions" value="${desiredServings}">
                                            <button type="submit" class="button is-info is-light">
                                                <span>🛒</span> <span class="ml-1">Añadir Faltantes a Compras</span>
                                            </button>
                                        </form>
                                    @endif
                                </div>
                            </div>
                        </div>
                    </div>
                @endfor
            </div>
        @endif
    `
)
