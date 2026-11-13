@param String active = ""

<aside class="menu sidebar-menu">
    <p class="menu-label">General</p>
    <ul class="menu-list">
        <li>
            <a href="/dashboard" class="${active.equals("dashboard") ? "is-active" : ""}">
                <span>📊</span> <span>Panel General</span>
            </a>
        </li>
    </ul>

    <p class="menu-label">Catálogo & Stock</p>
    <ul class="menu-list">
        <li>
            <a href="/catalog" class="${active.equals("catalog") ? "is-active" : ""}">
                <span>📦</span> <span>Catálogo Maestro</span>
            </a>
        </li>
        <li>
            <a href="/inventory" class="${active.equals("inventory") ? "is-active" : ""}">
                <span>🏷️</span> <span>Inventario & FEFO</span>
            </a>
        </li>
        <li>
            <a href="/inventory/locations" class="${active.equals("locations") ? "is-active" : ""}">
                <span>📍</span> <span>Ubicaciones</span>
            </a>
        </li>
        <li>
            <a href="/inventory/movements" class="${active.equals("movements") ? "is-active" : ""}">
                <span>📜</span> <span>Bitácora de Movimientos</span>
            </a>
        </li>
    </ul>

    <p class="menu-label">Compras</p>
    <ul class="menu-list">
        <li>
            <a href="/purchases/shopping-list" class="${active.equals("shopping-list") ? "is-active" : ""}">
                <span>🛒</span> <span>Lista de Compras</span>
            </a>
        </li>
        <li>
            <a href="/purchases/supermarket" class="${active.equals("supermarket") ? "is-active" : ""}">
                <span>📱</span> <span>Modo Supermercado</span>
            </a>
        </li>
        <li>
            <a href="/purchases/receipts/new" class="${active.equals("receipt-new") ? "is-active" : ""}">
                <span>🧾</span> <span>Ingresar Boleta</span>
            </a>
        </li>
        <li>
            <a href="/purchases/stores" class="${active.equals("stores") ? "is-active" : ""}">
                <span>🏪</span> <span>Comercios</span>
            </a>
        </li>
    </ul>

    <p class="menu-label">Cocina & Menús</p>
    <ul class="menu-list">
        <li>
            <a href="/recipes" class="${active.equals("recipes") ? "is-active" : ""}">
                <span>📖</span> <span>Recetario Familiar</span>
            </a>
        </li>
        <li>
            <a href="/recipes/what-to-cook" class="${active.equals("what-to-cook") ? "is-active" : ""}">
                <span>🍳</span> <span>¿Qué Cocinar Hoy?</span>
            </a>
        </li>
    </ul>

    <p class="menu-label">Analíticas & Datos</p>
    <ul class="menu-list">
        <li>
            <a href="/analytics" class="${active.equals("analytics") ? "is-active" : ""}">
                <span>📈</span> <span>Consumo & Precios</span>
            </a>
        </li>
        <li>
            <a href="/data/import" class="${active.equals("data-import") ? "is-active" : ""}">
                <span>📥</span> <span>Importación Masiva</span>
            </a>
        </li>
        <li>
            <a href="/data/export" class="${active.equals("data-export") ? "is-active" : ""}">
                <span>📤</span> <span>Exportación de Datos</span>
            </a>
        </li>
    </ul>
</aside>
