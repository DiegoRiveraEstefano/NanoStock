@import cl.diego.NanoStock.portfolio.dto.CreatorProfileResponse
@import cl.diego.NanoStock.ui.ViewContextHelper

@param CreatorProfileResponse creator = null

!{boolean authenticated = ViewContextHelper.isAuthenticated();}

@template.layouts.public(
    title = "Control Inteligente de Despensa y Caducidades",
    activePage = "landing",
    content = @`
        <!-- Hero Section -->
        <section class="hero hero-nano is-medium">
            <div class="hero-body">
                <div class="container">
                    <div class="columns is-vcentered is-variable is-8">
                        <div class="column is-7">
                            <span class="tag is-primary is-light is-medium is-rounded mb-4 has-text-weight-semibold">
                                ✨ Gestión Doméstica FEFO • Spring Modulith
                            </span>
                            <h1 class="title is-1 has-text-dark" style="line-height: 1.15;">
                                Controla tu despensa, reduce el desperdicio y cocina mejor
                            </h1>
                            <p class="subtitle is-5 has-text-grey-dark mt-4" style="line-height: 1.6;">
                                NanoStock organiza tus ubicaciones de almacenamiento, prioriza automáticamente los lotes
                                próximos a caducar (<strong>FEFO</strong>), agiliza tus compras en el supermercado y te
                                sugiere qué cocinar con lo que ya tienes en casa.
                            </p>

                            <div class="buttons are-medium mt-5">
                                @if(authenticated)
                                    <a href="/dashboard" class="button is-primary">
                                        <strong>Ir a mi Dashboard →</strong>
                                    </a>
                                    <a href="/about" class="button is-light">
                                        Ver Arquitectura
                                    </a>
                                @else
                                    <a href="/register" class="button is-primary">
                                        <strong>Comenzar Gratis</strong>
                                    </a>
                                    <a href="/login" class="button is-light">
                                        Iniciar Sesión
                                    </a>
                                    <a href="/about" class="button is-white">
                                        Acerca del Proyecto
                                    </a>
                                @endif
                            </div>
                        </div>

                        <div class="column is-5">
                            <div class="box p-5" style="border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 20px 40px rgba(0, 184, 148, 0.08);">
                                <div class="is-flex is-justify-content-space-between is-align-items-center mb-4">
                                    <span class="has-text-weight-bold has-text-dark">📊 Vista Previa de Despensa</span>
                                    <span class="tag is-success is-light is-rounded">FEFO Activo</span>
                                </div>

                                <div class="columns is-mobile is-multiline mb-3">
                                    <div class="column is-6">
                                        <div class="p-3 has-background-light" style="border-radius: 10px;">
                                            <p class="is-size-7 has-text-grey has-text-weight-semibold">UBICACIONES</p>
                                            <p class="is-size-4 has-text-weight-bold has-text-dark">4 Espacios</p>
                                            <p class="is-size-7 has-text-grey">Despensa, Refri, Freezer</p>
                                        </div>
                                    </div>
                                    <div class="column is-6">
                                        <div class="p-3 has-background-warning-light" style="border-radius: 10px;">
                                            <p class="is-size-7 has-text-grey has-text-weight-semibold">POR VENCER</p>
                                            <p class="is-size-4 has-text-weight-bold has-text-warning-dark">3 Lotes</p>
                                            <p class="is-size-7 has-text-grey">Prioridad de consumo</p>
                                        </div>
                                    </div>
                                </div>

                                <div class="notification is-primary is-light py-3 px-4 mb-3">
                                    <p class="is-size-7 has-text-weight-bold mb-1">🍳 Sugerencia Inteligente</p>
                                    <p class="is-size-7">
                                        Puedes preparar <strong>Arroz con Pollo y Verduras</strong> usando lotes que caducan esta semana.
                                    </p>
                                </div>

                                <div class="is-flex is-justify-content-space-between is-align-items-center pt-2" style="border-top: 1px solid #edf2f7;">
                                    <span class="is-size-7 has-text-grey">🛒 Modo Supermercado listo</span>
                                    <span class="tag is-info is-light is-small">CSV / JSON Export</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Características Principales -->
        <section class="section">
            <div class="container">
                <div class="has-text-centered mb-6">
                    <span class="tag is-info is-light is-rounded mb-2 has-text-weight-semibold">Funcionalidades</span>
                    <h2 class="title is-3">Todo el ciclo del hogar en un solo lugar</h2>
                    <p class="subtitle is-6 has-text-grey">
                        Desde que compras en el supermercado hasta que cocinas o analizas tus consumos y gastos.
                    </p>
                </div>

                <div class="columns is-multiline">
                    <div class="column is-4">
                        <div class="box feature-card h-100">
                            <div class="is-size-2 mb-3">🏷️</div>
                            <h3 class="title is-5 mb-2">Catálogo y Escáner</h3>
                            <p class="has-text-grey is-size-6">
                                Registra productos con código de barras, categoría, marca, unidad de medida y umbral mínimo de reposición.
                            </p>
                        </div>
                    </div>

                    <div class="column is-4">
                        <div class="box feature-card h-100">
                            <div class="is-size-2 mb-3">📦</div>
                            <h3 class="title is-5 mb-2">Inventario FEFO y Ubicaciones</h3>
                            <p class="has-text-grey is-size-6">
                                Gestiona despensas, refrigeradores y congeladores. Descuenta automáticamente el lote más próximo a vencer.
                            </p>
                        </div>
                    </div>

                    <div class="column is-4">
                        <div class="box feature-card h-100">
                            <div class="is-size-2 mb-3">🛒</div>
                            <h3 class="title is-5 mb-2">Compras y Modo Súper</h3>
                            <p class="has-text-grey is-size-6">
                                Crea listas de compras, marca productos en el pasillo con el Modo Supermercado y registra boletas con historial de precios.
                            </p>
                        </div>
                    </div>

                    <div class="column is-4">
                        <div class="box feature-card h-100">
                            <div class="is-size-2 mb-3">🍳</div>
                            <h3 class="title is-5 mb-2">¿Qué Cocino Hoy?</h3>
                            <p class="has-text-grey is-size-6">
                                Descubre recetas viables según tu stock real, detecta ingredientes faltantes y prioriza productos cercanos a caducar.
                            </p>
                        </div>
                    </div>

                    <div class="column is-4">
                        <div class="box feature-card h-100">
                            <div class="is-size-2 mb-3">📊</div>
                            <h3 class="title is-5 mb-2">Analítica y Alertas Quartz</h3>
                            <p class="has-text-grey is-size-6">
                                Visualiza mermas, gastos mensuales y valor del inventario, con alertas automáticas programadas para caducidades y stock crítico.
                            </p>
                        </div>
                    </div>

                    <div class="column is-4">
                        <div class="box feature-card h-100">
                            <div class="is-size-2 mb-3">🔁</div>
                            <h3 class="title is-5 mb-2">Importación y Exportación</h3>
                            <p class="has-text-grey is-size-6">
                                Exporta tu catálogo, stock y movimientos en JSON o CSV y realiza cargas masivas de productos sin fricción.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- Banner Creador / Proyecto -->
        <section class="section pt-0">
            <div class="container">
                <div class="box p-6 has-background-white" style="border-radius: 14px; border: 1px solid #e2e8f0;">
                    <div class="columns is-vcentered">
                        <div class="column is-8">
                            <h3 class="title is-4 mb-2">Proyecto Open Source & Portafolio</h3>
                            <p class="has-text-grey">
                                Diseñado con arquitectura modular en Java 21 y Spring Boot 4
                                @if(creator != null)
                                    por <strong>${creator.getFullName()}</strong>.
                                @else
                                    .
                                @endif
                                Explora los detalles técnicos o conoce quién está detrás del proyecto.
                            </p>
                        </div>
                        <div class="column is-4 has-text-right-tablet">
                            <div class="buttons is-right">
                                <a href="/about" class="button is-light">Acerca de NanoStock</a>
                                <a href="/creator" class="button is-primary is-light">Sobre el Creador</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    `
)
