@import java.util.List
@import cl.diego.NanoStock.demo.dto.DemoStatusDto
@import cl.diego.NanoStock.iam.dto.UserResponseDto
@import cl.diego.NanoStock.iam.model.Role
@import cl.diego.NanoStock.ui.ViewContextHelper

@param UserResponseDto user
@param boolean isAdmin = false
@param List<UserResponseDto> familyMembers = null
@param Role[] roles
@param String flashMessage = null
@param String flashType = "is-success"

!{DemoStatusDto demoStatus = ViewContextHelper.currentDemoStatus();}

@template.layouts.app(
    title = "Mi Perfil",
    activeSection = "profile",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Mi Perfil y Seguridad</h1>
                    <p class="subtitle is-6 has-text-grey">Información de tu cuenta familiar y credenciales de acceso.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="/dashboard" class="button is-light">← Volver al Panel</a>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = flashType.equals("is-danger") ? "⚠️" : "✅")
        @endif

        <div class="columns is-multiline">
            <!-- Datos de la Cuenta -->
            <div class="column is-5">
                <div class="box">
                    <h2 class="title is-5 mb-3">👤 Datos de Cuenta</h2>
                    <table class="table is-fullwidth is-striped mb-0">
                        <tbody>
                            <tr>
                                <th class="has-text-grey">Usuario</th>
                                <td class="has-text-weight-bold">${user.getUsername()}</td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">Correo</th>
                                <td>${user.getEmail()}</td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">Rol en el Hogar</th>
                                <td>
                                    @if(user.getRole() == Role.ROLE_ADMIN)
                                        <span class="tag is-primary is-light is-rounded">Administrador</span>
                                    @elseif(user.getRole() == Role.ROLE_MEMBER)
                                        <span class="tag is-info is-light is-rounded">Miembro</span>
                                    @else
                                        <span class="tag is-light is-rounded">Invitado</span>
                                    @endif
                                </td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">Estado</th>
                                <td>
                                    @if(user.isEnabled())
                                        <span class="tag is-success is-light is-rounded">Activa</span>
                                    @else
                                        <span class="tag is-danger is-light is-rounded">Deshabilitada</span>
                                    @endif
                                </td>
                            </tr>
                            @if(user.getCreatedAt() != null)
                                <tr>
                                    <th class="has-text-grey">Miembro desde</th>
                                    <td class="is-size-7">${user.getCreatedAt().toLocalDate().toString()}</td>
                                </tr>
                            @endif
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Cambiar Contraseña -->
            <div class="column is-7">
                <div class="box">
                    <h2 class="title is-5 mb-3">🔒 Cambiar Contraseña</h2>
                    <form action="/profile/change-password" method="post">
                        <div class="field">
                            <label class="label" for="currentPassword">Contraseña Actual <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <input id="currentPassword" class="input" type="password" name="currentPassword" required>
                            </div>
                        </div>

                        <div class="columns">
                            <div class="column is-6">
                                <div class="field">
                                    <label class="label" for="newPassword">Nueva Contraseña <span class="has-text-danger">*</span></label>
                                    <div class="control">
                                        <input id="newPassword" class="input" type="password" name="newPassword" minlength="6" placeholder="Mínimo 6 caracteres" required>
                                    </div>
                                </div>
                            </div>
                            <div class="column is-6">
                                <div class="field">
                                    <label class="label" for="confirmPassword">Confirmar Nueva Contraseña <span class="has-text-danger">*</span></label>
                                    <div class="control">
                                        <input id="confirmPassword" class="input" type="password" name="confirmPassword" minlength="6" required>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="field mt-3">
                            <button type="submit" class="button is-primary">Actualizar Contraseña</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Estado del Modo Demo (Solo ADMIN cuando está activo) -->
            @if(isAdmin && demoStatus != null && demoStatus.isEnabled())
                <div class="column is-12">
                    <div class="box">
                        <div class="level mb-3">
                            <div class="level-left">
                                <div>
                                    <h2 class="title is-5 mb-1">🧪 Administración de Modo Demo (Ciclo de ${demoStatus.getResetIntervalDays()} días)</h2>
                                    <p class="is-size-7 has-text-grey">
                                        Preserva los datos semilla, tu perfil de creador y cuentas ADMIN; elimina automáticamente datos nuevos de visitantes.
                                    </p>
                                </div>
                            </div>
                            <div class="level-right">
                                <form action="/profile/demo-reset" method="post"
                                      onsubmit="return confirm('¿Limpiar ahora todos los datos nuevos y restaurar la semilla demo?');">
                                    <button type="submit" class="button is-warning is-light has-text-weight-semibold">
                                        🔄 Ejecutar Limpieza Ahora
                                    </button>
                                </form>
                            </div>
                        </div>

                        <div class="columns is-mobile is-multiline">
                            <div class="column is-3">
                                <p class="is-size-7 has-text-grey">Último Reinicio</p>
                                <p class="has-text-weight-bold">${demoStatus.getFormattedLastResetAt()}</p>
                            </div>
                            <div class="column is-3">
                                <p class="is-size-7 has-text-grey">Próximo Reinicio Automático</p>
                                <p class="has-text-weight-bold">${demoStatus.getFormattedNextResetAt()}</p>
                            </div>
                            <div class="column is-3">
                                <p class="is-size-7 has-text-grey">Tiempo Restante</p>
                                <p class="has-text-weight-bold">${demoStatus.getDaysRemaining()} día(s) (${demoStatus.getHoursRemaining()}h)</p>
                            </div>
                            <div class="column is-3">
                                <p class="is-size-7 has-text-grey">Última Limpieza</p>
                                <p class="has-text-weight-bold">${demoStatus.getLastCleanupDeletedRows()} registro(s) eliminados</p>
                            </div>
                        </div>
                    </div>
                </div>
            @endif

            <!-- Gestión de Miembros (Solo ADMIN) -->
            @if(isAdmin && familyMembers != null)
                <div class="column is-12">
                    <div class="box p-0" style="overflow-x: auto;">
                        <div class="p-3 has-background-light is-flex is-justify-content-space-between is-align-items-center">
                            <h2 class="title is-6 mb-0">👨‍👩‍👧‍👦 Miembros del Hogar (Administración)</h2>
                            <span class="tag is-info is-light">${familyMembers.size()} usuarios</span>
                        </div>
                        <table class="table is-striped is-hoverable is-fullwidth mb-0">
                            <thead>
                                <tr>
                                    <th>Usuario</th>
                                    <th>Correo</th>
                                    <th>Rol</th>
                                    <th>Estado</th>
                                    <th class="has-text-right">Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                @for(UserResponseDto member : familyMembers)
                                    <tr>
                                        <td class="has-text-weight-bold">
                                            ${member.getUsername()}
                                            @if(member.getUsername().equals(user.getUsername()))
                                                <span class="tag is-small is-primary is-light ml-1">Tú</span>
                                            @endif
                                        </td>
                                        <td>${member.getEmail()}</td>
                                        <td>
                                            <form action="/profile/users/${member.getId()}/role" method="post" class="is-flex is-align-items-center gap-2">
                                                <div class="select is-small">
                                                    <select name="role" onchange="this.form.submit()">
                                                        @for(Role r : roles)
                                                            <option value="${r.name()}" selected="${member.getRole() == r}">${r.name()}</option>
                                                        @endfor
                                                    </select>
                                                </div>
                                            </form>
                                        </td>
                                        <td>
                                            @if(member.isEnabled())
                                                <span class="tag is-success is-light is-rounded">Activo</span>
                                            @else
                                                <span class="tag is-danger is-light is-rounded">Deshabilitado</span>
                                            @endif
                                        </td>
                                        <td class="has-text-right">
                                            @if(!member.getUsername().equals(user.getUsername()))
                                                <form action="/profile/users/${member.getId()}/toggle-enabled" method="post" style="display: inline;">
                                                    <button type="submit" class="button is-small ${member.isEnabled() ? "is-danger is-light" : "is-success is-light"}">
                                                        ${member.isEnabled() ? "Deshabilitar" : "Habilitar"}
                                                    </button>
                                                </form>
                                            @else
                                                <span class="is-size-7 has-text-grey">-</span>
                                            @endif
                                        </td>
                                    </tr>
                                @endfor
                            </tbody>
                        </table>
                    </div>
                </div>
            @endif
        </div>
    `
)
