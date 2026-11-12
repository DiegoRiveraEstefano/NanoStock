@import cl.diego.NanoStock.purchases.dto.ShoppingListResponse
@import cl.diego.NanoStock.purchases.dto.ShoppingListItemResponse

@param ShoppingListResponse shoppingList

@template.layouts.app(
    title = "Modo Supermercado",
    activeSection = "supermarket",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">🛒 Modo "En el Supermercado"</h1>
                    <p class="subtitle is-6 has-text-grey">Diseño optimizado para pantallas táctiles y navegación en pasillos.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="/purchases/shopping-list" class="button is-light">← Modo Tabla Tradicional</a>
            </div>
        </div>

        <div class="notification is-info is-light p-3 mb-4">
            <span class="mr-2">💡</span>
            <span>Toca el botón <strong>"En Carrito"</strong> a medida que ingresas artículos a tu carro físico.</span>
        </div>

        @if(shoppingList.getItems() == null || shoppingList.getItems().isEmpty())
            <div class="box has-text-centered p-5">
                <span class="is-size-1">🎉</span>
                <h2 class="title is-5 mt-2">¡No hay artículos pendientes!</h2>
                <p class="has-text-grey">Tu lista de compras está completamente al día.</p>
                <a href="/purchases/shopping-list" class="button is-primary is-light mt-4">Volver a la Lista</a>
            </div>
        @else
            <div class="columns is-multiline">
                @for(ShoppingListItemResponse item : shoppingList.getItems())
                    <div class="column is-6-tablet is-4-desktop">
                        <nano-shopping-item 
                            item-id="${item.getId().toString()}" 
                            status="${item.getItemStatus() != null ? item.getItemStatus().name() : "PENDING"}"
                            class="box p-4 is-flex is-flex-direction-column is-justify-content-space-between ${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "has-background-light is-in-cart" : ""}" 
                            style="min-height: 160px; border-left: 5px solid ${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "#00b894" : "#0984e3"};">
                            
                            <div>
                                <div class="is-flex is-justify-content-space-between is-align-items-start mb-2">
                                    <h3 class="title is-5 mb-0 item-title" style="${item.getItemStatus() != null && item.getItemStatus().name().equals("IN_CART") ? "text-decoration: line-through;" : ""}">
                                        ${item.getProductName()}
                                    </h3>
                                    <span class="tag is-primary is-light is-size-6 has-text-weight-bold">
                                        x${item.getSuggestedQuantity()}
                                    </span>
                                </div>
                                <div class="is-size-7 has-text-grey mb-3">
                                    <code>${item.getProductBarCode()}</code>
                                </div>
                            </div>

                            <div class="buttons are-small is-flex is-justify-content-space-between mt-2">
                                <button type="button" class="button is-success is-light has-text-weight-bold" data-action="toggle-cart">
                                    <span>🛒</span> <span class="ml-1">En Carrito</span>
                                </button>
                                <button type="button" class="button is-danger is-inverted" data-action="mark-out" title="No hay stock en góndola">
                                    <span>❌</span> <span class="ml-1">Agotado</span>
                                </button>
                            </div>
                        </nano-shopping-item>
                    </div>
                @endfor
            </div>
        @endif
    `,
    scripts = @`
        <script type="module" src="/js/components/nano-shopping-item.js"></script>
    `
)
