@import java.util.List
@import cl.diego.NanoStock.catalog.dto.ProductResponse
@import cl.diego.NanoStock.catalog.dto.CategoryResponse
@import cl.diego.NanoStock.catalog.dto.BrandResponse

@param List<ProductResponse> products
@param List<CategoryResponse> categories
@param List<BrandResponse> brands = List.of()
@param Long selectedCategory = null
@param String query = ""
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Catálogo Maestro",
    activeSection = "catalog",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Catálogo Maestro de Productos</h1>
                    <p class="subtitle is-6 has-text-grey">Definición canónica de artículos que consume el hogar.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <button type="button" class="button is-light" onclick="document.getElementById('modal-catalog-meta').open()">
                        <span>🏷️</span>
                        <span class="ml-1">Categorías y Marcas</span>
                    </button>
                    <nano-barcode-scanner target-input="#filterSearch" button-text="Buscar por Escáner"></nano-barcode-scanner>
                    <a href="/catalog/new" class="button is-primary">
                        <span>➕</span>
                        <span class="ml-1">Nuevo Producto</span>
                    </a>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = flashType.equals("is-danger") ? "⚠️" : "✅")
        @endif

        <!-- Filtros y Búsqueda -->
        <div class="box p-3 mb-4">
            <form action="/catalog" method="get">
                <div class="field is-grouped is-grouped-multiline">
                    <div class="control has-icons-left is-expanded">
                        <input id="filterSearch" class="input" type="text" name="query" placeholder="Buscar por nombre, SKU o código de barras..." value="${query}">
                        <span class="icon is-left">🔍</span>
                    </div>

                    <div class="control">
                        <div class="select">
                            <select name="categoryId" onchange="this.form.submit()">
                                <option value="">Todas las Categorías</option>
                                @for(CategoryResponse cat : categories)
                                    <option value="${cat.getId()}" selected="${selectedCategory != null && selectedCategory.equals(cat.getId())}">
                                        ${cat.getName()}
                                    </option>
                                @endfor
                            </select>
                        </div>
                    </div>

                    <div class="control">
                        <button type="submit" class="button is-info">Filtrar</button>
                        @if(!query.isEmpty() || selectedCategory != null)
                            <a href="/catalog" class="button is-light">Limpiar</a>
                        @endif
                    </div>
                </div>
            </form>
        </div>

        <!-- Tabla de Productos -->
        <div class="box p-0" style="overflow-x: auto;">
            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr class="has-background-light">
                        <th>Producto</th>
                        <th>Categoría</th>
                        <th>Marca</th>
                        <th>Código / SKU</th>
                        <th class="has-text-centered">UOM</th>
                        <th class="has-text-centered">Stock Mín / Meta</th>
                        <th class="has-text-centered">Tipo</th>
                        <th class="has-text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    @if(products.isEmpty())
                        <tr>
                            <td colspan="8" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">📦</span>
                                <p class="mt-2">No se encontraron productos registrados.</p>
                                <a href="/catalog/new" class="button is-small is-primary is-light mt-3">Registrar Primer Producto</a>
                            </td>
                        </tr>
                    @else
                        @for(ProductResponse p : products)
                            <tr>
                                <td>
                                    <a href="/catalog/${p.getId()}" class="has-text-weight-bold has-text-dark">
                                        ${p.getName()}
                                    </a>
                                    @if(p.getDescription() != null && !p.getDescription().isBlank())
                                        <div class="is-size-7 has-text-grey">${p.getDescription()}</div>
                                    @endif
                                </td>
                                <td>
                                    <span class="tag is-light is-info">
                                        ${p.getCategoryName() != null ? p.getCategoryName() : "Sin categoría"}
                                    </span>
                                </td>
                                <td>
                                    <span class="is-size-7">${p.getBrandName() != null ? p.getBrandName() : "Genérico"}</span>
                                </td>
                                <td>
                                    <div><code class="is-size-7">${p.getBarCode()}</code></div>
                                    <div class="is-size-7 has-text-grey">${p.getSku() != null ? p.getSku() : ""}</div>
                                </td>
                                <td class="has-text-centered">
                                    <span class="tag is-rounded is-small">
                                        ${p.getUnitOfMeasure() != null ? p.getUnitOfMeasure().getSymbol() : "u"}
                                    </span>
                                </td>
                                <td class="has-text-centered">
                                    <span class="has-text-danger has-text-weight-semibold">${p.getMinStockThreshold()}</span>
                                    <span class="has-text-grey-light">/</span>
                                    <span class="has-text-primary has-text-weight-semibold">${p.getTargetStockQuantity()}</span>
                                </td>
                                <td class="has-text-centered">
                                    @if(p.isPerishable())
                                        <span class="tag is-warning is-light is-rounded" title="Producto perecible con control FEFO">Perecible</span>
                                    @else
                                        <span class="tag is-light is-rounded" title="No perecible">Duradero</span>
                                    @endif
                                </td>
                                <td class="has-text-right">
                                    <div class="buttons are-small is-right">
                                        <a href="/catalog/${p.getId()}" class="button is-light" title="Ver Detalle">👁️</a>
                                        <a href="/catalog/${p.getId()}/edit" class="button is-light is-info" title="Editar">✏️</a>
                                    </div>
                                </td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>

        <!-- MODAL: Gestión de Categorías y Marcas -->
        <nano-modal id="modal-catalog-meta" title="Categorías y Marcas del Catálogo">
            <div class="columns">
                <div class="column is-6">
                    <h3 class="title is-6 mb-2">📂 Nueva Categoría</h3>
                    <form action="/catalog/categories" method="post" class="mb-3">
                        <input type="hidden" name="returnTo" value="/catalog">
                        <div class="field">
                            <input class="input is-small" type="text" name="name" placeholder="Nombre (Ej. Despensa)" required>
                        </div>
                        <div class="field">
                            <input class="input is-small" type="text" name="description" placeholder="Descripción (opcional)">
                        </div>
                        <button type="submit" class="button is-small is-primary is-fullwidth">Crear Categoría</button>
                    </form>
                    <p class="is-size-7 has-text-grey mb-1">Existentes (${categories.size()}):</p>
                    <div class="tags">
                        @for(CategoryResponse cat : categories)
                            <span class="tag is-info is-light">${cat.getName()}</span>
                        @endfor
                    </div>
                </div>

                <div class="column is-6">
                    <h3 class="title is-6 mb-2">🔖 Nueva Marca</h3>
                    <form action="/catalog/brands" method="post" class="mb-3">
                        <input type="hidden" name="returnTo" value="/catalog">
                        <div class="field">
                            <input class="input is-small" type="text" name="name" placeholder="Nombre (Ej. Colun)" required>
                        </div>
                        <button type="submit" class="button is-small is-info is-fullwidth">Crear Marca</button>
                    </form>
                    <p class="is-size-7 has-text-grey mb-1">Existentes (${brands != null ? brands.size() : 0}):</p>
                    <div class="tags">
                        @if(brands != null)
                            @for(BrandResponse brand : brands)
                                <span class="tag is-light">${brand.getName()}</span>
                            @endfor
                        @endif
                    </div>
                </div>
            </div>
            <div slot="footer">
                <button type="button" class="button" data-close>Cerrar</button>
            </div>
        </nano-modal>
    `,
    scripts = @`
        <script type="module" src="/js/components/nano-barcode-scanner.js"></script>
    `
)
