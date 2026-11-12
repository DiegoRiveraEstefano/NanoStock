@import java.util.List
@import cl.diego.NanoStock.purchases.dto.ShoppingListResponse
@import cl.diego.NanoStock.purchases.dto.ShoppingListItemResponse
@import cl.diego.NanoStock.catalog.dto.ProductResponse

@param ShoppingListResponse shoppingList
@param List<ProductResponse> allProducts
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Lista de Compras",
    activeSection = "shopping-list",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Lista de Compras Activa</h1>
                    <p class="subtitle is-6 has-text-grey">Planificación familiar y reposición automática de quiebres de stock.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <form action="/purchases/shopping-lists/suggest" method="post" class="is-inline">
                        <button type="submit" class="button is-info is-light">
                            <span>✨</span> <span class="ml-1">Sugerir Faltantes de Stock</span>
                        </button>
                    </form>
                    <a href="/purchases/supermarket" class="button is-primary">
                        <span>📱</span> <span class="ml-1">Modo Supermercado</span>
                    </a>
                    <button class="button is-light" onclick="document.getElementById('modal-add-item').open()">
                        <span>➕</span> <span class="ml-1">Agregar Ítem</span>
                    </button>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "✅")
        @endif

        <div class="box p-0" style="overflow-x: auto;">
            <div class="p-3 has-background-light is-flex is-justify-content-space-between is-align-items-center">
                <h2 class="title is-6 mb-0">${shoppingList.getTitle() != null ? shoppingList.getTitle() : "Lista Activa"}</h2>
                <span class="tag is-primary is-light">${shoppingList.getItems() != null ? shoppingList.getItems().size() : 0} artículos</span>
            </div>

            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr>
                        <th style="width: 40px;"></th>
                        <th>Producto</th>
                        <th>Código</th>
                        <th class="has-text-centered">Cantidad Sugerida</th>
                        <th class="has-text-centered">Origen</th>
                        <th class="has-text-centered">Estado</th>
                        <th class="has-text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    @if(shoppingList.getItems() == null || shoppingList.getItems().isEmpty())
                        <tr>
                            <td colspan="7" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">🛒</span>
                                <p class="mt-2">Tu lista de compras está vacía.</p>
                                <p class="is-size-7">Haz clic en <strong>"Sugerir Faltantes de Stock"</strong> para añadir automáticamente lo que hace falta en la despensa.</p>
                            </td>
                        </tr>
                    @else
                        @for(ShoppingListItemResponse item : shoppingList.getItems())
                            <tr class="${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "has-background-light" : ""}">
                                <td class="has-text-centered">
                                    <form action="/purchases/shopping-lists/items/${item.getId()}/status" method="post">
                                        <input type="hidden" name="status" value="${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "PENDING" : "IN_CART"}">
                                        <input type="checkbox" onchange="this.form.submit()" checked="${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART")}">
                                    </form>
                                </td>
                                <td>
                                    <span class="has-text-weight-bold ${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "has-text-grey" : "has-text-dark"}" style="${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "text-decoration: line-through;" : ""}">
                                        ${item.getProductName()}
                                    </span>
                                </td>
                                <td><code>${item.getProductBarCode()}</code></td>
                                <td class="has-text-centered has-text-weight-bold has-text-primary">
                                    ${item.getSuggestedQuantity()}
                                </td>
                                <td class="has-text-centered">
                                    @if(item.isAutoSuggested())
                                        <span class="tag is-info is-light is-rounded">🤖 Auto</span>
                                    @else
                                        <span class="tag is-light is-rounded">Manual</span>
                                    @endif
                                </td>
                                <td class="has-text-centered">
                                    @if(item.getItemStatus() != null)
                                        @if(item.getItemStatus().name().equals("IN_CART"))
                                            <span class="tag is-success is-rounded">En Carrito</span>
                                        @elseif(item.getItemStatus().name().equals("OUT_OF_STOCK"))
                                            <span class="tag is-danger is-light is-rounded">Agotado</span>
                                        @else
                                            <span class="tag is-warning is-light is-rounded">Pendiente</span>
                                        @endif
                                    @endif
                                </td>
                                <td class="has-text-right">
                                    <form action="/purchases/shopping-lists/items/${item.getId()}/delete" method="post" class="is-inline">
                                        <button type="submit" class="button is-small is-ghost has-text-danger p-0" title="Eliminar">🗑️</button>
                                    </form>
                                </td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>

        <!-- MODAL: Agregar Ítem Manualmente -->
        <nano-modal id="modal-add-item" title="Agregar Producto a la Lista">
            <form id="form-add-item" action="/purchases/shopping-lists/items" method="post">
                <div class="field">
                    <label class="label">Producto <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="productId" required>
                                <option value="">-- Selecciona un Producto --</option>
                                @for(ProductResponse p : allProducts)
                                    <option value="${p.getId()}">${p.getName()} (${p.getBarCode()})</option>
                                @endfor
                            </select>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Cantidad Requerida <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <input class="input" type="number" step="0.1" min="0.1" name="quantity" value="1.0" required>
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-add-item" class="button is-primary">Agregar a la Lista</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>
    `
)
