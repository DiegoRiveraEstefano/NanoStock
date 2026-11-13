@import java.util.List
@import cl.diego.NanoStock.portfolio.dto.CreatorProfileResponse
@import cl.diego.NanoStock.ui.ViewContextHelper

@param CreatorProfileResponse profile
@param String flashMessage = null
@param String flashType = "is-success"

!{boolean isAdmin = ViewContextHelper.isAdmin();}
!{List<String> techList = profile.getTechStackList();}
!{boolean hasSocialLinks = (profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank())
    || (profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank())
    || (profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank())
    || (profile.getXUrl() != null && !profile.getXUrl().isBlank())
    || (profile.getContactEmail() != null && !profile.getContactEmail().isBlank());}

@template.layouts.public(
    title = "Sobre el Creador",
    activePage = "creator",
    content = @`
        <section class="section">
            <div class="container" style="max-width: 960px;">
                @if(flashMessage != null)
                    @template.components.alert(message = flashMessage, type = flashType, icon = flashType.equals("is-danger") ? "⚠️" : "✅")
                @endif

                <div class="box p-6" style="border-radius: 16px;">
                    <div class="columns is-vcentered is-variable is-6">
                        <div class="column is-4 has-text-centered">
                            @if(profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank())
                                <figure class="image is-128x128 mx-auto mb-4">
                                    <img class="is-rounded creator-avatar-img" src="${profile.getAvatarUrl()}" alt="${profile.getFullName()}">
                                </figure>
                            @else
                                <div class="creator-avatar mx-auto mb-4">
                                    <span>👨‍💻</span>
                                </div>
                            @endif

                            <h1 class="title is-4 mb-1">${profile.getFullName()}</h1>
                            <p class="subtitle is-6 has-text-primary has-text-weight-semibold mt-1 mb-2">
                                ${profile.getHeadline()}
                            </p>

                            @if(profile.getLocation() != null && !profile.getLocation().isBlank())
                                <span class="tag is-light is-rounded">📍 ${profile.getLocation()}</span>
                            @endif

                            @if(isAdmin)
                                <div class="mt-4">
                                    <button type="button"
                                            class="button is-primary is-light is-small is-fullwidth"
                                            onclick="document.getElementById('modal-edit-creator').open()">
                                        <span>✏️</span>
                                        <span class="ml-1">Editar Mi Perfil y Redes</span>
                                    </button>
                                </div>
                            @endif
                        </div>

                        <div class="column is-8">
                            <div class="mb-5">
                                <h2 class="title is-5 mb-2">Quién Soy</h2>
                                <p class="has-text-grey-dark" style="line-height: 1.7; white-space: pre-line;">${profile.getBio()}</p>
                            </div>

                            @if(!techList.isEmpty())
                                <div class="mb-5">
                                    <h3 class="title is-6 mb-2">Stack & Tecnologías</h3>
                                    <div class="tags">
                                        @for(String tech : techList)
                                            <span class="tag is-primary is-light is-medium">${tech}</span>
                                        @endfor
                                    </div>
                                </div>
                            @endif

                            <div>
                                <h3 class="title is-6 mb-3">Redes Sociales y Contacto</h3>
                                @if(hasSocialLinks)
                                    <div class="buttons">
                                        @if(profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank())
                                            <a href="${profile.getGithubUrl()}" target="_blank" rel="noopener noreferrer" class="button social-pill is-dark is-outlined">
                                                <span>🐙</span>
                                                <span class="ml-2">GitHub</span>
                                            </a>
                                        @endif

                                        @if(profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank())
                                            <a href="${profile.getLinkedinUrl()}" target="_blank" rel="noopener noreferrer" class="button social-pill is-info is-outlined">
                                                <span>💼</span>
                                                <span class="ml-2">LinkedIn</span>
                                            </a>
                                        @endif

                                        @if(profile.getWebsiteUrl() != null && !profile.getWebsiteUrl().isBlank())
                                            <a href="${profile.getWebsiteUrl()}" target="_blank" rel="noopener noreferrer" class="button social-pill is-primary is-outlined">
                                                <span>🌐</span>
                                                <span class="ml-2">Sitio Web</span>
                                            </a>
                                        @endif

                                        @if(profile.getXUrl() != null && !profile.getXUrl().isBlank())
                                            <a href="${profile.getXUrl()}" target="_blank" rel="noopener noreferrer" class="button social-pill is-link is-outlined">
                                                <span>𝕏</span>
                                                <span class="ml-2">X / Twitter</span>
                                            </a>
                                        @endif

                                        @if(profile.getContactEmail() != null && !profile.getContactEmail().isBlank())
                                            <a href="mailto:${profile.getContactEmail()}" class="button social-pill is-success is-outlined">
                                                <span>✉️</span>
                                                <span class="ml-2">${profile.getContactEmail()}</span>
                                            </a>
                                        @endif
                                    </div>
                                @else
                                    <p class="is-size-7 has-text-grey">Aún no se han configurado enlaces de contacto.</p>
                                @endif
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        @if(isAdmin)
            <nano-modal id="modal-edit-creator" title="Editar Perfil de Creador y Redes">
                <form id="form-edit-creator" action="/creator" method="post">
                    <div class="columns">
                        <div class="column is-6">
                            <div class="field">
                                <label class="label">Nombre Completo <span class="has-text-danger">*</span></label>
                                <div class="control">
                                    <input class="input" type="text" name="fullName" value="${profile.getFullName()}" required maxlength="150">
                                </div>
                            </div>
                        </div>
                        <div class="column is-6">
                            <div class="field">
                                <label class="label">Ubicación</label>
                                <div class="control">
                                    <input class="input" type="text" name="location" value="${profile.getLocation() != null ? profile.getLocation() : ""}" placeholder="Ej. Santiago, Chile" maxlength="120">
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="field">
                        <label class="label">Titular Profesional <span class="has-text-danger">*</span></label>
                        <div class="control">
                            <input class="input" type="text" name="headline" value="${profile.getHeadline()}" required maxlength="200">
                        </div>
                    </div>

                    <div class="field">
                        <label class="label">Biografía / Quién Soy <span class="has-text-danger">*</span></label>
                        <div class="control">
                            <textarea class="textarea" rows="4" name="bio" required>${profile.getBio()}</textarea>
                        </div>
                    </div>

                    <div class="field">
                        <label class="label">URL de Avatar (opcional)</label>
                        <div class="control">
                            <input class="input" type="url" name="avatarUrl" value="${profile.getAvatarUrl() != null ? profile.getAvatarUrl() : ""}" placeholder="https://..." maxlength="500">
                        </div>
                    </div>

                    <div class="field">
                        <label class="label">Stack Tecnológico (separado por comas)</label>
                        <div class="control">
                            <input class="input" type="text" name="techStack" value="${profile.getTechStack() != null ? profile.getTechStack() : ""}" placeholder="Java 21, Spring Boot, SQLite, Docker" maxlength="500">
                        </div>
                    </div>

                    <hr class="my-4">
                    <p class="is-size-7 has-text-weight-bold has-text-grey is-uppercase mb-3">Redes Sociales y Contacto</p>

                    <div class="columns is-multiline">
                        <div class="column is-6">
                            <div class="field">
                                <label class="label is-small">GitHub URL</label>
                                <div class="control">
                                    <input class="input is-small" type="url" name="githubUrl" value="${profile.getGithubUrl() != null ? profile.getGithubUrl() : ""}" placeholder="https://github.com/usuario" maxlength="300">
                                </div>
                            </div>
                        </div>
                        <div class="column is-6">
                            <div class="field">
                                <label class="label is-small">LinkedIn URL</label>
                                <div class="control">
                                    <input class="input is-small" type="url" name="linkedinUrl" value="${profile.getLinkedinUrl() != null ? profile.getLinkedinUrl() : ""}" placeholder="https://linkedin.com/in/usuario" maxlength="300">
                                </div>
                            </div>
                        </div>
                        <div class="column is-6">
                            <div class="field">
                                <label class="label is-small">Sitio Web / Portafolio</label>
                                <div class="control">
                                    <input class="input is-small" type="url" name="websiteUrl" value="${profile.getWebsiteUrl() != null ? profile.getWebsiteUrl() : ""}" placeholder="https://tusitio.dev" maxlength="300">
                                </div>
                            </div>
                        </div>
                        <div class="column is-6">
                            <div class="field">
                                <label class="label is-small">X / Twitter URL</label>
                                <div class="control">
                                    <input class="input is-small" type="url" name="xUrl" value="${profile.getXUrl() != null ? profile.getXUrl() : ""}" placeholder="https://x.com/usuario" maxlength="300">
                                </div>
                            </div>
                        </div>
                        <div class="column is-12">
                            <div class="field">
                                <label class="label is-small">Correo de Contacto</label>
                                <div class="control">
                                    <input class="input is-small" type="email" name="contactEmail" value="${profile.getContactEmail() != null ? profile.getContactEmail() : ""}" placeholder="contacto@ejemplo.com" maxlength="200">
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
                <div slot="footer">
                    <button type="submit" form="form-edit-creator" class="button is-primary">Guardar Cambios</button>
                    <button type="button" class="button" data-close>Cancelar</button>
                </div>
            </nano-modal>
        @endif
    `
)
