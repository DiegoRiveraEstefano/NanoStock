@import gg.jte.Content

@param String title
@param Content content

@template.layouts.base(
    title = title,
    content = @`
        <section class="hero is-fullheight is-light">
            <div class="hero-body is-justify-content-center">
                <div class="container" style="max-width: 440px;">
                    <div class="has-text-centered mb-5">
                        <a href="/" class="is-inline-block" style="text-decoration: none;">
                            <span class="is-size-1">🥫</span>
                            <h1 class="title is-3 has-text-weight-bold has-text-primary mt-1">NanoStock</h1>
                        </a>
                        <p class="subtitle is-6 has-text-grey">Gestión Inteligente de Despensa y Caducidades</p>
                    </div>
                    <div class="box p-5">
                        ${content}
                    </div>
                    <div class="has-text-centered mt-4 is-size-7">
                        <a href="/" class="has-text-grey">← Volver al inicio</a>
                        <span class="mx-2 has-text-grey-light">•</span>
                        <a href="/about" class="has-text-grey">Acerca de</a>
                        <span class="mx-2 has-text-grey-light">•</span>
                        <a href="/creator" class="has-text-grey">Creador</a>
                    </div>
                </div>
            </div>
        </section>
    `
)
