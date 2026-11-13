@import gg.jte.Content
@import cl.diego.NanoStock.demo.dto.DemoStatusDto
@import cl.diego.NanoStock.ui.ViewContextHelper

@param String title
@param String activeSection = ""
@param int unreadNotifications = 0
@param String username = "Familia"
@param Content content
@param Content scripts = null
@param Content head = null

!{DemoStatusDto demoStatus = ViewContextHelper.currentDemoStatus();}

@template.layouts.base(
    title = title,
    head = head,
    scripts = scripts,
    content = @`
        @template.components.navbar(active = activeSection, unreadNotifications = unreadNotifications, username = username)

        <div class="main-wrapper">
            <aside class="sidebar-wrapper is-hidden-touch">
                @template.components.sidebar(active = activeSection)
            </aside>
            <main class="content-wrapper">
                @if(demoStatus != null && demoStatus.isEnabled())
                    <div class="notification is-info is-light py-2 px-4 mb-4 is-size-7 is-flex is-justify-content-space-between is-align-items-center">
                        <span>
                            🧪 <strong>Modo Demo Portafolio:</strong> puedes crear y probar datos libremente.
                            Los datos nuevos se reinician automáticamente cada <strong>${demoStatus.getResetIntervalDays()} días</strong>
                            (próxima limpieza: <code>${demoStatus.getFormattedNextResetAt()}</code>).
                        </span>
                        <button class="delete is-small" aria-label="close"></button>
                    </div>
                @endif
                ${content}
            </main>
        </div>
    `
)
