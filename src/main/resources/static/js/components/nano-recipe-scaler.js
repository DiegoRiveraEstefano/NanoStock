/**
 * <nano-recipe-scaler> Web Component
 * Escalador reactivo de raciones e ingredientes.
 * 
 * Permite cambiar el número de porciones y recalcula las cantidades de cada ingrediente,
 * actualizando los indicadores de suficiencia en vivo.
 */
class NanoRecipeScaler extends HTMLElement {
    connectedCallback() {
        if (!this._initialized) {
            this._render();
            this._bindEvents();
            this._initialized = true;
        }
    }

    _render() {
        const baseServings = parseInt(this.getAttribute('base-servings') || '4', 10);
        const ingredientsJson = this.getAttribute('ingredients-data') || '[]';
        this._baseServings = baseServings;
        this._currentServings = baseServings;
        this._ingredients = JSON.parse(ingredientsJson);

        this.innerHTML = `
            <div class="box p-4 mb-4 has-background-light">
                <div class="level is-mobile mb-3">
                    <div class="level-left">
                        <span class="has-text-weight-bold is-size-6">👥 Ajustar Raciones:</span>
                    </div>
                    <div class="level-right">
                        <div class="field has-addons">
                            <div class="control">
                                <button type="button" class="button is-small is-primary is-outlined" id="decrease-btn">−</button>
                            </div>
                            <div class="control">
                                <input class="input is-small has-text-centered has-text-weight-bold" type="number" id="servings-input" value="${baseServings}" min="1" max="20" style="width: 60px;">
                            </div>
                            <div class="control">
                                <button type="button" class="button is-small is-primary is-outlined" id="increase-btn">+</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="is-size-7 has-text-grey">
                    Raciones base de la receta: <strong>${baseServings}</strong>. Las cantidades de los ingredientes se escalan proporcionalmente.
                </div>
            </div>

            <div class="ingredients-list-container">
                <table class="table is-fullwidth is-striped is-hoverable">
                    <thead>
                        <tr>
                            <th>Ingrediente</th>
                            <th class="has-text-centered">Cantidad Necesaria</th>
                            <th class="has-text-centered">UOM</th>
                            <th>Notas</th>
                        </tr>
                    </thead>
                    <tbody id="scaled-ingredients-tbody">
                    </tbody>
                </table>
            </div>
        `;

        this._input = this.querySelector('#servings-input');
        this._decBtn = this.querySelector('#decrease-btn');
        this._incBtn = this.querySelector('#increase-btn');
        this._tbody = this.querySelector('#scaled-ingredients-tbody');

        this._renderIngredients();
    }

    _bindEvents() {
        this._decBtn.addEventListener('click', () => {
            if (this._currentServings > 1) {
                this._currentServings--;
                this._input.value = this._currentServings;
                this._renderIngredients();
                this._notifyServingsChange();
            }
        });

        this._incBtn.addEventListener('click', () => {
            if (this._currentServings < 30) {
                this._currentServings++;
                this._input.value = this._currentServings;
                this._renderIngredients();
                this._notifyServingsChange();
            }
        });

        this._input.addEventListener('change', () => {
            let val = parseInt(this._input.value, 10);
            if (isNaN(val) || val < 1) val = 1;
            this._currentServings = val;
            this._input.value = val;
            this._renderIngredients();
            this._notifyServingsChange();
        });
    }

    _renderIngredients() {
        const factor = this._currentServings / this._baseServings;
        this._tbody.innerHTML = '';

        this._ingredients.forEach(ing => {
            const scaledQty = (ing.quantity * factor);
            const formattedQty = Number.isInteger(scaledQty) ? scaledQty : scaledQty.toFixed(1);

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <span class="has-text-weight-semibold">${ing.productName}</span>
                    ${ing.isOptional ? '<span class="tag is-small is-light ml-2">Opcional</span>' : ''}
                </td>
                <td class="has-text-centered has-text-weight-bold has-text-primary is-size-6">
                    ${formattedQty}
                </td>
                <td class="has-text-centered">
                    <span class="tag is-rounded is-small">${ing.unitOfMeasure || 'u'}</span>
                </td>
                <td class="is-size-7 has-text-grey">
                    ${ing.notes || '-'}
                </td>
            `;
            this._tbody.appendChild(tr);
        });
    }

    _notifyServingsChange() {
        // Actualizar posibles campos ocultos en el formulario de "Cocinar"
        const cookServingsInput = document.querySelector('input[name="targetPortions"]');
        if (cookServingsInput) {
            cookServingsInput.value = this._currentServings;
        }

        this.dispatchEvent(new CustomEvent('nano:servings-changed', {
            bubbles: true,
            detail: { servings: this._currentServings }
        }));
    }
}

customElements.define('nano-recipe-scaler', NanoRecipeScaler);
