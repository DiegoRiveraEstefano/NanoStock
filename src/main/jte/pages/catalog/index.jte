@import java.util.List
@import cl.diego.NanoStock.catalog.dto.ProductResponse
@import cl.diego.NanoStock.catalog.dto.CategoryResponse

@param List<ProductResponse> products
@param List<CategoryResponse> categories
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
                    <nano-barcode-scanner target-input="#filterSearch" button-text="Buscar por Escáner"></nano-barcode-scanner>
                    <a href="/catalog/new" class="button is-primary">
                        <span>➕</span>
                        <span class="ml-1">Nuevo Producto</span>
                    </a>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "✅")
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
    `,
    scripts = @`
        <script type="module" src="/js/components/nano-barcode-scanner.js"></script>
    `
)
