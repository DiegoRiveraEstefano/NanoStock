@import java.util.List
@import cl.diego.NanoStock.notifications.dto.NotificationResponse

@param List<NotificationResponse> notifications
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Centro de Notificaciones",
    activeSection = "notifications",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Centro de Alertas & Notificaciones</h1>
                    <p class="subtitle is-6 has-text-grey">Avisos automáticos de caducidad temprana y quiebres de stock crítico.</p>
                </div>
            </div>
            <div class="level-right">
                <form action="/notifications/mark-all-read" method="post">
                    <button type="submit" class="button is-light">
                        <span>✔️</span> <span class="ml-1">Marcar Todas como Leídas</span>
                    </button>
                </form>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "✅")
        @endif

        @if(notifications.isEmpty())
            <div class="box has-text-centered p-5">
                <span class="is-size-1">🔔</span>
                <h2 class="title is-5 mt-2">¡No tienes notificaciones pendientes!</h2>
                <p class="has-text-grey">Tu despensa está bajo control y sin alertas prioritarias.</p>
            </div>
        @else
            <div class="box p-0">
                <table class="table is-striped is-hoverable is-fullwidth mb-0">
                    <thead>
                        <tr class="has-background-light">
                            <th>Fecha</th>
                            <th>Tipo</th>
                            <th>Severidad</th>
                            <th>Mensaje</th>
                            <th class="has-text-right">Acción</th>
                        </tr>
                    </thead>
                    <tbody>
                        @for(NotificationResponse n : notifications)
                            <tr class="${n.isRead() ? "has-text-grey" : "has-text-weight-semibold"}">
                                <td class="is-size-7">
                                    ${n.getCreatedAt() != null ? n.getCreatedAt().toString().replace("T", " ").substring(0, 16) : ""}
                                </td>
                                <td>
                                    <span class="tag is-small is-light">${n.getNotificationType() != null ? n.getNotificationType().name() : "GENERAL"}</span>
                                </td>
                                <td>
                                    @if(n.getSeverity() != null && n.getSeverity().name().equals("CRITICAL"))
                                        <span class="tag is-danger is-small is-rounded">Crítico</span>
                                    @elseif(n.getSeverity() != null && n.getSeverity().name().equals("WARNING"))
                                        <span class="tag is-warning is-small is-rounded">Aviso</span>
                                    @else
                                        <span class="tag is-info is-small is-rounded">Info</span>
                                    @endif
                                </td>
                                <td>
                                    <div>${n.getTitle()}</div>
                                    <div class="is-size-7 has-text-grey">${n.getMessage()}</div>
                                </td>
                                <td class="has-text-right">
                                    @if(!n.isRead())
                                        <form action="/notifications/${n.getId()}/read" method="post" class="is-inline">
                                            <button type="submit" class="button is-small is-primary is-light">Leído</button>
                                        </form>
                                    @else
                                        <span class="is-size-7 has-text-grey-light">Leído</span>
                                    @endif
                                </td>
                            </tr>
                        @endfor
                    </tbody>
                </table>
            </div>
        @endif
    `
)
