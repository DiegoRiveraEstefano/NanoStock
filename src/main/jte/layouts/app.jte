@import gg.jte.Content

@param String title
@param String activeSection = ""
@param int unreadNotifications = 0
@param String username = "Familia"
@param Content content
@param Content scripts = null
@param Content head = null

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
                ${content}
            </main>
        </div>
    `
)
