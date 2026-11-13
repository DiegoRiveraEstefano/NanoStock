@import gg.jte.Content
@import cl.diego.NanoStock.ui.ViewContextHelper

@param String title = "Inicio"
@param String activePage = ""
@param Content head = null
@param Content content
@param Content scripts = null

!{boolean authenticated = ViewContextHelper.isAuthenticated();}
!{String resolvedUsername = ViewContextHelper.currentUsername("Usuario");}

@template.layouts.base(
    title = title,
    head = head,
    content = @`
        <nav class="navbar is-nano" role="navigation" aria-label="public navigation">
            <div class="container">
                <div class="navbar-brand">
                    <a class="navbar-item" href="/">
                        <span class="is-size-4 mr-2">🥫</span>
                        <span class="brand-title">NanoStock</span>
                    </a>

                    <a role="button" class="navbar-burger" aria-label="menu" aria-expanded="false" data-target="nanoPublicNavbar">
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                        <span aria-hidden="true"></span>
                    </a>
                </div>

                <div id="nanoPublicNavbar" class="navbar-menu">
                    <div class="navbar-start">
                        <a class="navbar-item ${activePage.equals("landing") ? "is-active has-text-weight-bold has-text-primary" : ""}" href="/">
                            Inicio
                        </a>
                        <a class="navbar-item ${activePage.equals("about") ? "is-active has-text-weight-bold has-text-primary" : ""}" href="/about">
                            Acerca de
                        </a>
                        <a class="navbar-item ${activePage.equals("creator") ? "is-active has-text-weight-bold has-text-primary" : ""}" href="/creator">
                            Creador
                        </a>
                    </div>

                    <div class="navbar-end">
                        <div class="navbar-item">
                            <div class="buttons">
                                @if(authenticated)
                                    <a href="/profile" class="button is-light">
                                        <span>👤</span>
                                        <span class="ml-1">${resolvedUsername}</span>
                                    </a>
                                    <a href="/dashboard" class="button is-primary">
                                        <strong>Ir al Dashboard →</strong>
                                    </a>
                                @else
                                    <a href="/login" class="button is-light">
                                        Iniciar Sesión
                                    </a>
                                    <a href="/register" class="button is-primary">
                                        <strong>Crear Cuenta</strong>
                                    </a>
                                @endif
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </nav>

        <main style="flex: 1;">
            ${content}
        </main>

        <footer class="footer has-background-white py-5 mt-auto" style="border-top: 1px solid #eef2f5;">
            <div class="container">
                <div class="columns is-vcentered">
                    <div class="column is-6">
                        <p class="is-size-6 has-text-weight-bold has-text-dark">
                            🥫 NanoStock
                        </p>
                        <p class="is-size-7 has-text-grey mt-1">
                            Sistema inteligente de gestión doméstica de despensa, control FEFO, compras y recetas.
                        </p>
                    </div>
                    <div class="column is-6 has-text-right-tablet">
                        <div class="buttons is-right are-small">
                            <a href="/" class="button is-ghost has-text-grey">Inicio</a>
                            <a href="/about" class="button is-ghost has-text-grey">Acerca de</a>
                            <a href="/creator" class="button is-ghost has-text-grey">Creador</a>
                            @if(authenticated)
                                <a href="/dashboard" class="button is-ghost has-text-primary has-text-weight-semibold">Dashboard</a>
                            @else
                                <a href="/login" class="button is-ghost has-text-primary has-text-weight-semibold">Ingresar</a>
                            @endif
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    `,
    scripts = scripts
)
