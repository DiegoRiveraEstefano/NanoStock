@import java.util.List
@import cl.diego.NanoStock.catalog.dto.ProductResponse
@import cl.diego.NanoStock.catalog.model.UnitOfMeasure
@import cl.diego.NanoStock.recipes.dto.CreateRecipeRequest
@import cl.diego.NanoStock.recipes.dto.RecipeIngredientRequest
@import cl.diego.NanoStock.recipes.model.PortionType
@import cl.diego.NanoStock.recipes.model.RecipeCategory
@import cl.diego.NanoStock.recipes.model.RecipeDifficulty

@param CreateRecipeRequest form
@param Long recipeId = null
@param List<ProductResponse> products
@param UnitOfMeasure[] unitsOfMeasure
@param RecipeCategory[] categories
@param RecipeDifficulty[] difficulties
@param PortionType[] portionTypes
@param String errorMessage = null

!{List<RecipeIngredientRequest> initialIngredients = (form.getIngredients() != null && !form.getIngredients().isEmpty()) ? form.getIngredients() : List.of(new RecipeIngredientRequest());}

@template.layouts.app(
    title = recipeId == null ? "Nueva Receta" : "Editar Receta",
    activeSection = "recipes",
    content = @`
        <nav class="breadcrumb has-succeeds-separator is-small mb-4" aria-label="breadcrumbs">
            <ul>
                <li><a href="/recipes">Recetario</a></li>
                <li class="is-active"><a href="#" aria-current="page">${recipeId == null ? "Nueva Receta" : "Editar Receta"}</a></li>
            </ul>
        </nav>

        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">${recipeId == null ? "Registrar Nueva Receta" : "Editar Receta Familiar"}</h1>
                    <p class="subtitle is-6 has-text-grey">Define instrucciones, porciones e ingredientes vinculados a tu catálogo.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="${recipeId == null ? "/recipes" : "/recipes/" + recipeId}" class="button is-light">← Volver</a>
            </div>
        </div>

        @if(errorMessage != null)
            @template.components.alert(message = errorMessage, type = "is-danger", icon = "⚠️")
        @endif

        <div class="box">
            <form id="recipe-form" action="${recipeId == null ? "/recipes" : "/recipes/" + recipeId}" method="post">
                <div class="columns is-multiline">
                    <div class="column is-8">
                        <div class="field">
                            <label class="label">Título de la Receta <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <input class="input" type="text" name="title" value="${form.getTitle() != null ? form.getTitle() : ""}" placeholder="Ej. Arroz con Pollo Casero" required autofocus>
                            </div>
                        </div>
                    </div>

                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Categoría</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="category">
                                        @for(RecipeCategory cat : categories)
                                            <option value="${cat.name()}" selected="${form.getCategory() != null && form.getCategory().equals(cat)}">${cat.name()}</option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="column is-12">
                        <div class="field">
                            <label class="label">Descripción Breve</label>
                            <div class="control">
                                <input class="input" type="text" name="description" value="${form.getDescription() != null ? form.getDescription() : ""}" placeholder="Ej. Plato rendidor ideal para aprovechar verduras frescas">
                            </div>
                        </div>
                    </div>

                    <div class="column is-3">
                        <div class="field">
                            <label class="label">Raciones Base <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <input class="input" type="number" min="1" name="servings" value="${form.getServings() != null ? form.getServings().toString() : "4"}" required>
                            </div>
                        </div>
                    </div>

                    <div class="column is-3">
                        <div class="field">
                            <label class="label">Tipo de Porción</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="portionType">
                                        @for(PortionType pt : portionTypes)
                                            <option value="${pt.name()}" selected="${form.getPortionType() != null && form.getPortionType().equals(pt)}">${pt.name()}</option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="column is-3">
                        <div class="field">
                            <label class="label">Dificultad</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="difficulty">
                                        @for(RecipeDifficulty diff : difficulties)
                                            <option value="${diff.name()}" selected="${form.getDifficulty() != null && form.getDifficulty().equals(diff)}">${diff.name()}</option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="column is-3">
                        <div class="field">
                            <label class="label">Tiempo Prep. (min)</label>
                            <div class="control">
                                <input class="input" type="number" min="1" name="prepTimeMinutes" value="${form.getPrepTimeMinutes() != null ? form.getPrepTimeMinutes().toString() : "30"}">
                            </div>
                        </div>
                    </div>

                    <!-- Ingredientes dinámicos -->
                    <div class="column is-12">
                        <div class="is-flex is-justify-content-space-between is-align-items-center mb-2">
                            <label class="label mb-0">Ingredientes de la Receta <span class="has-text-danger">*</span></label>
                            <button type="button" id="btn-add-ingredient" class="button is-small is-info is-light">
                                <span>➕</span> <span class="ml-1">Añadir Ingrediente</span>
                            </button>
                        </div>

                        @if(products.isEmpty())
                            <div class="notification is-warning is-light is-size-7">
                                No hay productos en el catálogo. <a href="/catalog/new">Registra productos primero</a> para asociarlos a recetas.
                            </div>
                        @endif

                        <div id="ingredients-container" class="p-3 has-background-light" style="border-radius: 8px;">
                            @for(int i = 0; i < initialIngredients.size(); i++)
                                !{RecipeIngredientRequest ing = initialIngredients.get(i);}
                                <div class="columns is-vcentered is-mobile is-multiline ingredient-row mb-2 pb-2" style="border-bottom: 1px solid #e2e8f0;">
                                    <div class="column is-5">
                                        <label class="is-size-7 has-text-grey">Producto</label>
                                        <div class="select is-small is-fullwidth">
                                            <select data-field="productId" name="ingredients[${i}].productId" required>
                                                <option value="">-- Selecciona producto --</option>
                                                @for(ProductResponse p : products)
                                                    <option value="${p.getId()}" selected="${ing.getProductId() != null && ing.getProductId().equals(p.getId())}">
                                                        ${p.getName()} (${p.getUnitOfMeasure() != null ? p.getUnitOfMeasure().getSymbol() : "u"})
                                                    </option>
                                                @endfor
                                            </select>
                                        </div>
                                    </div>
                                    <div class="column is-2">
                                        <label class="is-size-7 has-text-grey">Cantidad</label>
                                        <input class="input is-small" type="number" step="0.01" min="0.01"
                                               data-field="quantity" name="ingredients[${i}].quantity"
                                               value="${ing.getQuantity() != null ? ing.getQuantity().toString() : "1.0"}" required>
                                    </div>
                                    <div class="column is-2">
                                        <label class="is-size-7 has-text-grey">Unidad</label>
                                        <div class="select is-small is-fullwidth">
                                            <select data-field="unitOfMeasure" name="ingredients[${i}].unitOfMeasure">
                                                @for(UnitOfMeasure uom : unitsOfMeasure)
                                                    <option value="${uom.name()}" selected="${ing.getUnitOfMeasure() != null && ing.getUnitOfMeasure().equals(uom)}">
                                                        ${uom.getSymbol()} (${uom.name()})
                                                    </option>
                                                @endfor
                                            </select>
                                        </div>
                                    </div>
                                    <div class="column is-2 has-text-centered">
                                        <label class="checkbox is-size-7 mt-4">
                                            <input type="checkbox" data-field="optional" name="ingredients[${i}].optional" value="true" checked="${ing.isOptional()}">
                                            Opcional
                                        </label>
                                    </div>
                                    <div class="column is-1 has-text-right">
                                        <button type="button" class="button is-small is-danger is-light mt-3 btn-remove-ingredient" title="Quitar">✖</button>
                                    </div>
                                </div>
                            @endfor
                        </div>
                    </div>

                    <div class="column is-12">
                        <div class="field">
                            <label class="label">Instrucciones de Preparación <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <textarea class="textarea" rows="5" name="instructions" placeholder="1. Preparar ingredientes...&#10;2. Cocinar..." required>${form.getInstructions() != null ? form.getInstructions() : ""}</textarea>
                            </div>
                        </div>
                    </div>

                    <div class="column is-12">
                        <div class="field">
                            <label class="label">Notas Culinarias</label>
                            <div class="control">
                                <textarea class="textarea has-fixed-size" rows="2" name="notes" placeholder="Tips opcionales de cocción o sustitutos">${form.getNotes() != null ? form.getNotes() : ""}</textarea>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="field is-grouped mt-4">
                    <div class="control">
                        <button type="submit" class="button is-primary">
                            <span>💾</span>
                            <span class="ml-1">${recipeId == null ? "Guardar Receta" : "Guardar Cambios"}</span>
                        </button>
                    </div>
                    <div class="control">
                        <a href="${recipeId == null ? "/recipes" : "/recipes/" + recipeId}" class="button is-light">Cancelar</a>
                    </div>
                </div>
            </form>
        </div>
    `,
    scripts = @`
        <script>
            document.addEventListener('DOMContentLoaded', () => {
                const container = document.getElementById('ingredients-container');
                const addBtn = document.getElementById('btn-add-ingredient');
                const form = document.getElementById('recipe-form');

                function reindexRows() {
                    const rows = container.querySelectorAll('.ingredient-row');
                    rows.forEach((row, idx) => {
                        row.querySelectorAll('[data-field]').forEach(el => {
                            const field = el.getAttribute('data-field');
                            el.setAttribute('name', 'ingredients[' + idx + '].' + field);
                        });
                    });
                }

                function bindRemoveButtons() {
                    container.querySelectorAll('.btn-remove-ingredient').forEach(btn => {
                        btn.onclick = () => {
                            const rows = container.querySelectorAll('.ingredient-row');
                            if (rows.length > 1) {
                                btn.closest('.ingredient-row').remove();
                                reindexRows();
                            }
                        };
                    });
                }

                addBtn.addEventListener('click', () => {
                    const firstRow = container.querySelector('.ingredient-row');
                    if (!firstRow) return;
                    const clone = firstRow.cloneNode(true);
                    clone.querySelectorAll('input').forEach(inp => {
                        if (inp.type === 'checkbox') {
                            inp.checked = false;
                        } else if (inp.type === 'number') {
                            inp.value = '1.0';
                        } else {
                            inp.value = '';
                        }
                    });
                    clone.querySelectorAll('select').forEach(sel => {
                        sel.selectedIndex = 0;
                    });
                    container.appendChild(clone);
                    reindexRows();
                    bindRemoveButtons();
                });

                form.addEventListener('submit', reindexRows);
                bindRemoveButtons();
            });
        </script>
    `
)
