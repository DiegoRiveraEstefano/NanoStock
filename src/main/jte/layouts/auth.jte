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
                        <span class="is-size-1">🥫</span>
                        <h1 class="title is-3 has-text-weight-bold has-text-primary">NanoStock</h1>
                        <p class="subtitle is-6 has-text-grey">Gestión Inteligente de Despensa y Caducidades</p>
                    </div>
                    <div class="box p-5">
                        ${content}
                    </div>
                </div>
            </div>
        </section>
    `
)
