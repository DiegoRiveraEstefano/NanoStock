@import cl.diego.NanoStock.portfolio.dto.CreatorProfileResponse
@import cl.diego.NanoStock.ui.ViewContextHelper

@param CreatorProfileResponse creator = null

!{boolean authenticated = ViewContextHelper.isAuthenticated();}

@template.layouts.public(
    title = "Acerca de NanoStock",
    activePage = "about",
    content = @`
        <section class="hero is-primary is-light">
            <div class="hero-body">
                <div class="container">
                    <span class="tag is-primary is-rounded mb-3 has-text-weight-semibold">Sobre el Proyecto</span>
                    <h1 class="title is-2">Acerca de NanoStock</h1>
                    <p class="subtitle is-5 mt-2">
                        Una plataforma doméstica diseñada para eliminar el desperdicio de alimentos mediante trazabilidad FEFO
                        y una arquitectura Spring Modulith limpia y extensible.
                    </p>
                </div>
            </div>
        </section>

        <section class="section">
            <div class="container">
                <!-- Problema y Solución -->
                <div class="columns is-variable is-6 mb-6">
                    <div class="column is-6">
                        <div class="box h-100">
                            <h2 class="title is-4 mb-3">🎯 El Problema que Resuelve</h2>
                            <p class="mb-3 has-text-grey-dark">
                                En la mayoría de los hogares es difícil saber exactamente qué hay en la despensa, en qué ubicación
                                quedó guardado o qué producto está a punto de vencer. Esto provoca:
                            </p>
                            <div class="content">
                                <ul>
                                    <li>Desperdicio de alimentos por caducidad olvidada en el fondo del refrigerador o alacena.</li>
                                    <li>Compras duplicadas por no saber el stock real mientras se está en el supermercado.</li>
                                    <li>Falta de claridad sobre el gasto mensual en víveres y las mermas reales del hogar.</li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <div class="column is-6">
                        <div class="box h-100">
                            <h2 class="title is-4 mb-3">💡 La Solución FEFO</h2>
                            <p class="mb-3 has-text-grey-dark">
                                NanoStock aplica el principio logístico <strong>FEFO (First Expired, First Out)</strong> adaptado al hogar:
                            </p>
                            <div class="content">
                                <ul>
                                    <li>Cada ingreso de stock registra su lote, ubicación física y fecha de vencimiento.</li>
                                    <li>Al consumir un producto, el sistema descuenta automáticamente el lote con caducidad más próxima.</li>
                                    <li>Jobs programados con <strong>Quartz</strong> generan alertas tempranas de vencimiento y stock crítico.</li>
                                    <li>El motor de recetas prioriza ingredientes que requieren consumo urgente.</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Arquitectura Modular -->
                <div class="box p-5 mb-6">
                    <h2 class="title is-4 mb-2">🧩 Arquitectura Spring Modulith</h2>
                    <p class="has-text-grey mb-4">
                        El código está organizado en módulos de negocio desacoplados que se comunican mediante eventos de dominio
                        y servicios bien definidos:
                    </p>

                    <div class="columns is-multiline">
                        <div class="column is-4">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">🔐 iam</p>
                                <p class="is-size-7 has-text-grey">Usuarios, roles (Admin/Member/Guest), autenticación y perfil.</p>
                            </div>
                        </div>
                        <div class="column is-4">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">🏷️ catalog</p>
                                <p class="is-size-7 has-text-grey">Productos, códigos de barras, categorías, marcas y umbrales.</p>
                            </div>
                        </div>
                        <div class="column is-4">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">📦 inventory</p>
                                <p class="is-size-7 has-text-grey">Ubicaciones, lotes de stock, trazabilidad FEFO y movimientos.</p>
                            </div>
                        </div>
                        <div class="column is-4">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">🛒 purchases</p>
                                <p class="is-size-7 has-text-grey">Tiendas, listas de compra, boletas e historial de precios.</p>
                            </div>
                        </div>
                        <div class="column is-4">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">🍳 recipes</p>
                                <p class="is-size-7 has-text-grey">Recetas, ingredientes y recomendador "¿Qué cocino hoy?".</p>
                            </div>
                        </div>
                        <div class="column is-4">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">📊 analytics & notifications</p>
                                <p class="is-size-7 has-text-grey">Métricas de gasto/merma y alertas programadas con Quartz.</p>
                            </div>
                        </div>
                        <div class="column is-6">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">🔁 dataimport & dataexport</p>
                                <p class="is-size-7 has-text-grey">Respaldo y portabilidad completa en formatos CSV y JSON.</p>
                            </div>
                        </div>
                        <div class="column is-6">
                            <div class="p-3 has-background-light" style="border-radius: 8px;">
                                <p class="has-text-weight-bold">👨‍💻 portfolio</p>
                                <p class="is-size-7 has-text-grey">Vistas públicas, presentación del proyecto y perfil editable del creador.</p>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Stack Tecnológico -->
                <div class="box p-5 mb-6">
                    <h2 class="title is-4 mb-3">🛠️ Stack Tecnológico</h2>
                    <div class="tags are-medium">
                        <span class="tag is-primary is-light">Java 21</span>
                        <span class="tag is-primary is-light">Spring Boot 4.0</span>
                        <span class="tag is-primary is-light">Spring Modulith 2.0</span>
                        <span class="tag is-info is-light">Spring Security 7</span>
                        <span class="tag is-info is-light">Spring Data JPA</span>
                        <span class="tag is-warning is-light">SQLite + Flyway</span>
                        <span class="tag is-link is-light">JTE Templates</span>
                        <span class="tag is-link is-light">Bulma CSS</span>
                        <span class="tag is-link is-light">HTMX & Web Components</span>
                        <span class="tag is-success is-light">Quartz Scheduler</span>
                    </div>
                </div>

                <div class="has-text-centered">
                    <div class="buttons is-centered">
                        <a href="/creator" class="button is-primary">
                            Conocer al Creador @if(creator != null)(${creator.getFullName()})@endif
                        </a>
                        @if(authenticated)
                            <a href="/dashboard" class="button is-light">Ir al Dashboard</a>
                        @else
                            <a href="/register" class="button is-light">Crear Cuenta</a>
                        @endif
                    </div>
                </div>
            </div>
        </section>
    `
)
