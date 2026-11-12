@template.layouts.app(
    title = "Exportación de Datos",
    activeSection = "data-export",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Exportación de Datos del Hogar</h1>
                    <p class="subtitle is-6 has-text-grey">Descarga tus datos en formatos estándar Excel (.xlsx), CSV (RFC-4180) o JSON indentado.</p>
                </div>
            </div>
        </div>

        <div class="columns is-multiline">
            <!-- Exportar Catálogo -->
            <div class="column is-4">
                <div class="card" style="height: 100%; border-radius: 8px;">
                    <header class="card-header has-background-light">
                        <p class="card-header-title">📦 Catálogo Maestro</p>
                    </header>
                    <div class="card-content">
                        <p class="is-size-6 has-text-grey mb-4">
                            Descarga la totalidad de productos registrados, códigos de barras, unidades de medida y umbrales mínimos.
                        </p>
                        <div class="buttons are-small">
                            <a href="/api/export/products?format=EXCEL" class="button is-primary is-fullwidth mb-2" download>
                                📊 Descargar Excel (.xlsx)
                            </a>
                            <a href="/api/export/products?format=CSV" class="button is-light is-fullwidth mb-2" download>
                                📄 Descargar CSV (.csv)
                            </a>
                            <a href="/api/export/products?format=JSON" class="button is-light is-fullwidth" download>
                                🔧 Descargar JSON (.json)
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Exportar Inventario -->
            <div class="column is-4">
                <div class="card" style="height: 100%; border-radius: 8px;">
                    <header class="card-header has-background-light">
                        <p class="card-header-title">🏷️ Existencias & Lotes Físicos</p>
                    </header>
                    <div class="card-content">
                        <p class="is-size-6 has-text-grey mb-4">
                            Detalle de lotes activos en despensa, refrigerador y freezer, junto con sus fechas de vencimiento y días restantes.
                        </p>
                        <div class="buttons are-small">
                            <a href="/api/export/inventory?format=EXCEL" class="button is-primary is-fullwidth mb-2" download>
                                📊 Descargar Excel (.xlsx)
                            </a>
                            <a href="/api/export/inventory?format=CSV" class="button is-light is-fullwidth mb-2" download>
                                📄 Descargar CSV (.csv)
                            </a>
                            <a href="/api/export/inventory?format=JSON" class="button is-light is-fullwidth" download>
                                🔧 Descargar JSON (.json)
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Exportar Movimientos / Auditoría -->
            <div class="column is-4">
                <div class="card" style="height: 100%; border-radius: 8px;">
                    <header class="card-header has-background-light">
                        <p class="card-header-title">📜 Bitácora de Auditoría</p>
                    </header>
                    <div class="card-content">
                        <p class="is-size-6 has-text-grey mb-4">
                            Historial completo e inmutable de entradas, consumos familiares, bajas por merma y ajustes físicos realizados.
                        </p>
                        <div class="buttons are-small">
                            <a href="/api/export/movements?format=EXCEL" class="button is-primary is-fullwidth mb-2" download>
                                📊 Descargar Excel (.xlsx)
                            </a>
                            <a href="/api/export/movements?format=CSV" class="button is-light is-fullwidth mb-2" download>
                                📄 Descargar CSV (.csv)
                            </a>
                            <a href="/api/export/movements?format=JSON" class="button is-light is-fullwidth" download>
                                🔧 Descargar JSON (.json)
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
)
