@import java.util.List
@import cl.diego.NanoStock.purchases.dto.StoreResponse
@import cl.diego.NanoStock.purchases.model.PaymentMethod
@import cl.diego.NanoStock.catalog.dto.ProductResponse
@import cl.diego.NanoStock.inventory.dto.LocationResponse

@param List<StoreResponse> stores
@param List<ProductResponse> products
@param List<LocationResponse> locations
@param String productsJson
@param String locationsJson
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Ingresar Boleta de Compra",
    activeSection = "receipt-new",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Registrar Boleta / Factura de Compra</h1>
                    <p class="subtitle is-6 has-text-grey">Asienta compras para alimentar el historial de precios y cargar stock en inventario automáticamente.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="/purchases/stores" class="button is-light">Ver Comercios</a>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "⚠️")
        @endif

        <div class="box">
            <form action="/purchases/receipts" method="post">
                <div class="columns is-multiline">
                    <!-- Comercio -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Comercio / Tienda <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="storeId" required>
                                        <option value="">-- Selecciona el Comercio --</option>
                                        @for(StoreResponse s : stores)
                                            <option value="${s.getId()}">${s.getName()} (${s.getBranchName() != null ? s.getBranchName() : s.getStoreType().toString()})</option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Fecha de Compra -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Fecha de Compra <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <input class="input" type="date" name="purchaseDate" required>
                            </div>
                        </div>
                    </div>

                    <!-- Método de Pago -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Método de Pago</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="paymentMethod">
                                        <option value="DEBIT_CARD">Tarjeta de Débito</option>
                                        <option value="CREDIT_CARD">Tarjeta de Crédito</option>
                                        <option value="CASH">Efectivo</option>
                                        <option value="TRANSFER">Transferencia</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Notas -->
                    <div class="column is-12">
                        <div class="field">
                            <label class="label">Observaciones / N° de Boleta</label>
                            <div class="control">
                                <input class="input" type="text" name="notes" placeholder="Ej. Boleta 104593, compra mensual supermercado">
                            </div>
                        </div>
                    </div>
                </div>

                <hr>

                <!-- Constructor de Líneas de Boleta -->
                <h2 class="title is-5 mb-3">Detalle de Artículos Comprados</h2>
                <nano-receipt-builder 
                    products-data="${productsJson}" 
                    locations-data="${locationsJson}">
                </nano-receipt-builder>

                <div class="field is-grouped mt-5">
                    <div class="control">
                        <button type="submit" class="button is-primary">
                            <span>💾</span> <span class="ml-1">Registrar Boleta e Ingresar Stock</span>
                        </button>
                    </div>
                    <div class="control">
                        <a href="/purchases/shopping-list" class="button is-light">Cancelar</a>
                    </div>
                </div>
            </form>
        </div>
    `,
    scripts = @`
        <script type="module" src="/js/components/nano-receipt-builder.js"></script>
    `
)
