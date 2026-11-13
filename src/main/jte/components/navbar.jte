@import cl.diego.NanoStock.ui.ViewContextHelper

@param String active = ""
@param int unreadNotifications = 0
@param String username = "Familia"

!{String resolvedUsername = ViewContextHelper.currentUsername(username);}
!{int resolvedUnread = ViewContextHelper.currentUnreadCount(unreadNotifications);}

<nav class="navbar is-nano" role="navigation" aria-label="main navigation">
    <div class="navbar-brand">
        <a class="navbar-item" href="/dashboard">
            <span class="is-size-4 mr-2">🥫</span>
            <span class="brand-title">NanoStock</span>
        </a>

        <a role="button" class="navbar-burger" aria-label="menu" aria-expanded="false" data-target="nanoNavbar">
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
        </a>
    </div>

    <div id="nanoNavbar" class="navbar-menu">
        <div class="navbar-start">
            <a class="navbar-item ${active.equals("dashboard") ? "is-active has-text-weight-bold" : ""}" href="/dashboard">
                Inicio
            </a>
            <a class="navbar-item ${active.equals("inventory") || active.equals("locations") || active.equals("movements") ? "is-active has-text-weight-bold" : ""}" href="/inventory">
                Inventario
            </a>
            <a class="navbar-item ${active.equals("shopping-list") || active.equals("supermarket") || active.equals("receipt-new") || active.equals("stores") ? "is-active has-text-weight-bold" : ""}" href="/purchases/shopping-list">
                Compras
            </a>
            <a class="navbar-item ${active.equals("recipes") || active.equals("what-to-cook") ? "is-active has-text-weight-bold" : ""}" href="/recipes">
                Recetas
            </a>
        </div>

        <div class="navbar-end">
            <div class="navbar-item">
                <a class="button is-light is-rounded mr-2" href="/notifications" title="Notificaciones y Alertas">
                    <span>🔔</span>
                    @if(resolvedUnread > 0)
                        <span class="tag is-danger is-rounded ml-1">${resolvedUnread}</span>
                    @endif
                </a>

                <div class="dropdown is-right">
                    <div class="dropdown-trigger">
                        <button class="button is-white" aria-haspopup="true" aria-controls="dropdown-user">
                            <span class="mr-2">👤</span>
                            <span class="has-text-weight-medium">${resolvedUsername}</span>
                            <span class="is-size-7 ml-1">▼</span>
                        </button>
                    </div>
                    <div class="dropdown-menu" id="dropdown-user" role="menu">
                        <div class="dropdown-content">
                            <a href="/profile" class="dropdown-item">Mi Perfil</a>
                            <a href="/data/export" class="dropdown-item">Exportar Datos</a>
                            <a href="/data/import" class="dropdown-item">Importar Datos</a>
                            <hr class="dropdown-divider">
                            <a href="/" class="dropdown-item">Página Principal</a>
                            <a href="/about" class="dropdown-item">Acerca de NanoStock</a>
                            <a href="/creator" class="dropdown-item">Sobre el Creador</a>
                            <hr class="dropdown-divider">
                            <form action="/logout" method="post" class="px-3 py-1">
                                <button type="submit" class="button is-small is-ghost has-text-danger p-0">Cerrar Sesión</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</nav>
