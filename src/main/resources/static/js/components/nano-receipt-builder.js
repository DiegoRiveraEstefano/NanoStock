/**
 * <nano-receipt-builder> Web Component
 * Formulario reactivo para registrar boletas de compra con cálculo en vivo de totales.
 */
class NanoReceiptBuilder extends HTMLElement {
    connectedCallback() {
        if (!this._initialized) {
            this._render();
            this._bindEvents();
            this._initialized = true;
        }
    }

    _render() {
        const productsJson = this.getAttribute('products-data') || '[]';
        const locationsJson = this.getAttribute('locations-data') || '[]';
        this._products = JSON.parse(productsJson);
        this._locations = JSON.parse(locationsJson);

        this.innerHTML = `
            <div class="receipt-lines-container mb-4">
                <table class="table is-fullwidth is-hoverable">
                    <thead>
                        <tr>
                            <th style="width: 32%;">Producto</th>
                            <th style="width: 15%;">Ubicación</th>
                            <th style="width: 12%;">Cantidad</th>
                            <th style="width: 14%;">Precio Unit. ($)</th>
                            <th style="width: 12%;">Descuento ($)</th>
                            <th style="width: 15%; text-align: right;">Subtotal ($)</th>
                            <th style="width: 5%;"></th>
                        </tr>
                    </thead>
                    <tbody id="receipt-items-tbody">
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="5" class="has-text-right has-text-weight-bold is-size-5">Total Calculado:</td>
                            <td class="has-text-right has-text-weight-bold is-size-5 has-text-primary" id="receipt-total-display">$0</td>
                            <td></td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div class="buttons">
                <button type="button" class="button is-small is-primary is-light" id="add-row-btn">
                    <span>➕</span> <span class="ml-1">Agregar Producto</span>
                </button>
            </div>
        `;

        this._tbody = this.querySelector('#receipt-items-tbody');
        this._totalDisplay = this.querySelector('#receipt-total-display');
        this._addRowBtn = this.querySelector('#add-row-btn');

        // Agregar una primera fila por defecto
        this._addRow();
    }

    _bindEvents() {
        this._addRowBtn.addEventListener('click', () => this._addRow());

        this._tbody.addEventListener('input', (e) => {
            if (e.target.matches('.item-qty, .item-price, .item-discount')) {
                this._recalculateRow(e.target.closest('tr'));
                this._recalculateTotal();
            }
        });

        this._tbody.addEventListener('click', (e) => {
            if (e.target.closest('.delete-row-btn')) {
                const row = e.target.closest('tr');
                if (this._tbody.querySelectorAll('tr').length > 1) {
                    row.remove();
                    this._reindexRows();
                    this._recalculateTotal();
                } else {
                    if (window.nanoToast) window.nanoToast('La boleta debe tener al menos una línea', 'is-warning');
                }
            }
        });
    }

    _addRow() {
        const index = this._tbody.querySelectorAll('tr').length;
        const tr = document.createElement('tr');

        let productOptions = '<option value="">-- Selecciona --</option>';
        this._products.forEach(p => {
            productOptions += `<option value="${p.id}">${p.name} (${p.barCode})</option>`;
        });

        let locationOptions = '<option value="">-- Por defecto --</option>';
        this._locations.forEach(l => {
            locationOptions += `<option value="${l.id}">${l.name}</option>`;
        });

        tr.innerHTML = `
            <td>
                <div class="select is-fullwidth is-small">
                    <select name="items[${index}].productId" required>
                        ${productOptions}
                    </select>
                </div>
            </td>
            <td>
                <div class="select is-fullwidth is-small">
                    <select name="items[${index}].locationId">
                        ${locationOptions}
                    </select>
                </div>
            </td>
            <td>
                <input class="input is-small item-qty" type="number" step="0.1" min="0.1" name="items[${index}].quantity" value="1.0" required>
            </td>
            <td>
                <input class="input is-small item-price" type="number" step="0.01" min="0" name="items[${index}].unitPrice" value="0.00" required>
            </td>
            <td>
                <input class="input is-small item-discount" type="number" step="0.01" min="0" name="items[${index}].discountAmount" value="0.00">
            </td>
            <td class="has-text-right has-text-weight-semibold item-subtotal-display">
                $0.00
            </td>
            <td class="has-text-centered">
                <button type="button" class="delete is-small delete-row-btn" aria-label="Eliminar fila"></button>
            </td>
        `;

        this._tbody.appendChild(tr);
        this._recalculateRow(tr);
        this._recalculateTotal();
    }

    _recalculateRow(row) {
        const qty = parseFloat(row.querySelector('.item-qty')?.value) || 0;
        const price = parseFloat(row.querySelector('.item-price')?.value) || 0;
        const discount = parseFloat(row.querySelector('.item-discount')?.value) || 0;

        const subtotal = Math.max(0, (qty * price) - discount);
        const subtotalDisplay = row.querySelector('.item-subtotal-display');
        if (subtotalDisplay) {
            subtotalDisplay.textContent = '$' + subtotal.toFixed(2);
        }
    }

    _recalculateTotal() {
        let grandTotal = 0;
        this._tbody.querySelectorAll('tr').forEach(row => {
            const qty = parseFloat(row.querySelector('.item-qty')?.value) || 0;
            const price = parseFloat(row.querySelector('.item-price')?.value) || 0;
            const discount = parseFloat(row.querySelector('.item-discount')?.value) || 0;
            grandTotal += Math.max(0, (qty * price) - discount);
        });

        if (this._totalDisplay) {
            this._totalDisplay.textContent = '$' + grandTotal.toFixed(2);
        }
    }

    _reindexRows() {
        const rows = this._tbody.querySelectorAll('tr');
        rows.forEach((row, i) => {
            row.querySelectorAll('[name]').forEach(input => {
                const name = input.getAttribute('name');
                input.setAttribute('name', name.replace(/items\[\d+\]/, `items[${i}]`));
            });
        });
    }
}

customElements.define('nano-receipt-builder', NanoReceiptBuilder);
