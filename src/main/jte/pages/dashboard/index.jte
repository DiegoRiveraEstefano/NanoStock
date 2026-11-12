@param int totalProducts = 0
@param int criticalStockCount = 0
@param int expiringSoonCount = 0
@param int shoppingListCount = 0
@param int unreadNotifications = 0
@param String username = "Familia"

@template.layouts.app(
    title = "Panel General",
    activeSection = "dashboard",
    unreadNotifications = unreadNotifications,
    username = username,
    content = @`
        <div class="level mb-5">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">¡Hola, ${username}! 👋</h1>
                    <p class="subtitle is-6 has-text-grey">Resumen del estado de la despensa y alertas prioritarias.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <a href="/catalog" class="button is-primary is-light">
                        <span>📦</span> <span class="ml-1">Ver Catálogo</span>
                    </a>
                    <a href="/purchases/shopping-list" class="button is-primary">
                        <span>🛒</span> <span class="ml-1">Ir al Supermercado</span>
                    </a>
                </div>
            </div>
        </div>

        @if(expiringSoonCount > 0)
            @template.components.alert(
                message = "Hay " + expiringSoonCount + " productos con fecha de vencimiento próxima (menos de 7 días). ¡Revisa el inventario para evitar desperdicios!",
                type = "is-warning",
                icon = "⚠️"
            )
        @endif

        @if(criticalStockCount > 0)
            @template.components.alert(
                message = "Hay " + criticalStockCount + " productos por debajo del umbral mínimo de seguridad.",
                type = "is-danger",
                icon = "🚨"
            )
        @endif

        <!-- KPIs / Resumen -->
        <div class="columns is-multiline mb-5">
            <div class="column is-3">
                @template.components.stat_card(
                    title = "Productos en Catálogo",
                    value = String.valueOf(totalProducts),
                    icon = "📦",
                    helperText = "Artículos registrados",
                    colorClass = "has-text-primary",
                    link = "/catalog"
                )
            </div>
            <div class="column is-3">
                @template.components.stat_card(
                    title = "Por Caducar (7 días)",
                    value = String.valueOf(expiringSoonCount),
                    icon = "⏳",
                    helperText = "Prioridad de consumo FEFO",
                    colorClass = expiringSoonCount > 0 ? "has-text-warning" : "has-text-grey",
                    link = "/inventory"
                )
            </div>
            <div class="column is-3">
                @template.components.stat_card(
                    title = "Bajo Stock Crítico",
                    value = String.valueOf(criticalStockCount),
                    icon = "📉",
                    helperText = "Requieren reposición",
                    colorClass = criticalStockCount > 0 ? "has-text-danger" : "has-text-grey",
                    link = "/purchases/shopping-list"
                )
            </div>
            <div class="column is-3">
                @template.components.stat_card(
                    title = "Lista de Compras",
                    value = String.valueOf(shoppingListCount),
                    icon = "🛒",
                    helperText = "Artículos pendientes",
                    colorClass = "has-text-info",
                    link = "/purchases/shopping-list"
                )
            </div>
        </div>

        <!-- Accesos Rápidos & Asistente Culinario -->
        <div class="columns">
            <div class="column is-8">
                <div class="box">
                    <div class="level mb-3">
                        <div class="level-left">
                            <h2 class="title is-5 mb-0">🍳 Sugerencia: ¿Qué cocinar hoy?</h2>
                        </div>
                        <div class="level-right">
                            <a href="/recipes/what-to-cook" class="button is-small is-ghost">Explorar Recetas →</a>
                        </div>
                    </div>
                    <p class="has-text-grey mb-4">
                        El motor de recetas analiza tus ingredientes físicos disponibles y prioriza aquellos lotes que están más próximos a caducar para minimizar el desperdicio familiar.
                    </p>
                    <a href="/recipes/what-to-cook" class="button is-primary is-outlined is-fullwidth">
                        Consultar recetas viables con ingredientes de hoy
                    </a>
                </div>
            </div>

            <div class="column is-4">
                <div class="box">
                    <h2 class="title is-5 mb-3">⚡ Acciones Rápidas</h2>
                    <div class="buttons are-small">
                        <a href="/inventory" class="button is-fullwidth is-light mb-2 is-justify-content-flex-start">
                            <span>➕ Ingresar Lote de Stock</span>
                        </a>
                        <a href="/purchases/receipts/new" class="button is-fullwidth is-light mb-2 is-justify-content-flex-start">
                            <span>🧾 Registrar Boleta de Compra</span>
                        </a>
                        <a href="/data/import" class="button is-fullwidth is-light mb-2 is-justify-content-flex-start">
                            <span>📥 Importar Datos Masivos</span>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    `
)
