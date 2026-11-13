@import cl.diego.NanoStock.iam.dto.UserRegistrationRequestDto

@param UserRegistrationRequestDto form = null
@param String errorMessage = null

!{String usernameVal = (form != null && form.getUsername() != null) ? form.getUsername() : "";}
!{String emailVal = (form != null && form.getEmail() != null) ? form.getEmail() : "";}

@template.layouts.auth(
    title = "Crear Cuenta",
    content = @`
        <h2 class="title is-4 has-text-centered mb-4">Crear Cuenta</h2>

        @if(errorMessage != null)
            @template.components.alert(message = errorMessage, type = "is-danger", icon = "⚠️")
        @endif

        <form action="/register" method="post">
            <div class="field">
                <label class="label" for="username">Nombre de Usuario <span class="has-text-danger">*</span></label>
                <div class="control has-icons-left">
                    <input id="username"
                           class="input"
                           type="text"
                           name="username"
                           value="${usernameVal}"
                           minlength="3"
                           maxlength="50"
                           placeholder="Ej. diego_rivera"
                           required
                           autofocus>
                    <span class="icon is-small is-left">👤</span>
                </div>
                <p class="help">Entre 3 y 50 caracteres.</p>
            </div>

            <div class="field">
                <label class="label" for="email">Correo Electrónico <span class="has-text-danger">*</span></label>
                <div class="control has-icons-left">
                    <input id="email"
                           class="input"
                           type="email"
                           name="email"
                           value="${emailVal}"
                           placeholder="tu@correo.com"
                           required>
                    <span class="icon is-small is-left">✉️</span>
                </div>
            </div>

            <div class="field">
                <label class="label" for="password">Contraseña <span class="has-text-danger">*</span></label>
                <div class="control has-icons-left">
                    <input id="password"
                           class="input"
                           type="password"
                           name="password"
                           minlength="6"
                           placeholder="Mínimo 6 caracteres"
                           required>
                    <span class="icon is-small is-left">🔒</span>
                </div>
            </div>

            <div class="field">
                <label class="label" for="confirmPassword">Confirmar Contraseña <span class="has-text-danger">*</span></label>
                <div class="control has-icons-left">
                    <input id="confirmPassword"
                           class="input"
                           type="password"
                           name="confirmPassword"
                           minlength="6"
                           placeholder="Repite tu contraseña"
                           required>
                    <span class="icon is-small is-left">🔒</span>
                </div>
            </div>

            <div class="field mt-5">
                <button type="submit" class="button is-primary is-fullwidth has-text-weight-semibold">
                    Registrarme
                </button>
            </div>
        </form>

        <hr class="my-4">

        <p class="has-text-centered is-size-6 has-text-grey">
            ¿Ya tienes una cuenta?
            <a href="/login" class="has-text-primary has-text-weight-semibold">Iniciar sesión</a>
        </p>
    `
)
