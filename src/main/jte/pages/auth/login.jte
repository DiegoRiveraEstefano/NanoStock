@param String errorMessage = null
@param String infoMessage = null
@param String successMessage = null
@param boolean demoEnabled = false
@param String demoUsername = "demo"
@param String demoPassword = "demo1234"
@param int demoResetDays = 7

@template.layouts.auth(
    title = "Iniciar Sesión",
    content = @`
        <h2 class="title is-4 has-text-centered mb-4">Iniciar Sesión</h2>

        @if(errorMessage != null)
            @template.components.alert(message = errorMessage, type = "is-danger", icon = "⚠️")
        @endif

        @if(successMessage != null)
            @template.components.alert(message = successMessage, type = "is-success", icon = "✅")
        @endif

        @if(infoMessage != null)
            @template.components.alert(message = infoMessage, type = "is-info", icon = "ℹ️")
        @endif

        <form action="/login" method="post">
            <div class="field">
                <label class="label" for="usernameOrEmail">Usuario o Correo Electrónico</label>
                <div class="control has-icons-left">
                    <input id="usernameOrEmail"
                           class="input"
                           type="text"
                           name="usernameOrEmail"
                           placeholder="Ej. diego o diego@familia.cl"
                           required
                           autofocus>
                    <span class="icon is-small is-left">👤</span>
                </div>
            </div>

            <div class="field">
                <label class="label" for="password">Contraseña</label>
                <div class="control has-icons-left">
                    <input id="password"
                           class="input"
                           type="password"
                           name="password"
                           placeholder="Tu contraseña"
                           required>
                    <span class="icon is-small is-left">🔒</span>
                </div>
            </div>

            <div class="field mt-5">
                <button type="submit" class="button is-primary is-fullwidth has-text-weight-semibold">
                    Entrar a NanoStock
                </button>
            </div>
        </form>

        @if(demoEnabled)
            <div class="notification is-info is-light mt-4 mb-0 p-4">
                <p class="is-size-7 has-text-weight-bold mb-1">🚀 Modo Demo Portafolio</p>
                <p class="is-size-7 mb-3">
                    Explora la app con datos precargados (usuario: <code>${demoUsername}</code>).
                    Los datos nuevos se limpian automáticamente cada <strong>${demoResetDays} días</strong>.
                </p>
                <form action="/login" method="post">
                    <input type="hidden" name="usernameOrEmail" value="${demoUsername}">
                    <input type="hidden" name="password" value="${demoPassword}">
                    <button type="submit" class="button is-info is-small is-fullwidth has-text-weight-semibold">
                        Entrar como Usuario Demo (1 clic)
                    </button>
                </form>
            </div>
        @endif

        <hr class="my-4">

        <p class="has-text-centered is-size-6 has-text-grey">
            ¿Aún no tienes cuenta?
            <a href="/register" class="has-text-primary has-text-weight-semibold">Crear cuenta familiar</a>
        </p>
    `
)
