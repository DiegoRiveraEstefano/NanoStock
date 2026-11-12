@import cl.diego.NanoStock.catalog.dto.ProductResponse
@import cl.diego.NanoStock.inventory.dto.ProductStockOverview
@import cl.diego.NanoStock.inventory.dto.StockBatchResponse

@param ProductResponse product
@param ProductStockOverview stockOverview = null
@param String flashMessage = null

@template.layouts.app(
    title = product.getName(),
    activeSection = "catalog",
    content = @`
        <nav class="breadcrumb has-succeeds-separator is-small mb-4" aria-label="breadcrumbs">
            <ul>
                <li><a href="/catalog">Catálogo</a></li>
                <li class="is-active"><a href="#" aria-current="page">${product.getName()}</a></li>
            </ul>
        </nav>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = "is-success", icon = "✅")
        @endif

        <div class="level mb-5">
            <div class="level-left">
                <div>
                    <div class="is-flex is-align-items-center gap-2 mb-1">
                        <h1 class="title is-4 mb-0">${product.getName()}</h1>
                        @if(product.isPerishable())
                            <span class="tag is-warning is-light is-rounded">Perecible (FEFO)</span>
                        @else
                            <span class="tag is-light is-rounded">No perecible</span>
                        @endif
                    </div>
                    <p class="subtitle is-6 has-text-grey">
                        ${product.getCategoryName() != null ? product.getCategoryName() : "Sin categoría"} · 
                        ${product.getBrandName() != null ? product.getBrandName() : "Genérico"}
                    </p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <a href="/catalog/${product.getId()}/edit" class="button is-info is-light">
                        <span>✏️</span> <span class="ml-1">Editar</span>
                    </a>
                    <a href="/inventory" class="button is-primary">
                        <span>➕</span> <span class="ml-1">Gestionar Stock</span>
                    </a>
                </div>
            </div>
        </div>

        <!-- Ficha Técnica -->
        <div class="columns">
            <div class="column is-4">
                <div class="box">
                    <h2 class="title is-6 mb-3">Identificación Maestra</h2>
                    <table class="table is-fullwidth is-narrow is-striped mb-0">
                        <tbody>
                            <tr>
                                <th class="has-text-grey">Código de Barras</th>
                                <td><code>${product.getBarCode()}</code></td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">SKU</th>
                                <td>${product.getSku() != null ? product.getSku() : "N/A"}</td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">Unidad de Medida</th>
                                <td>
                                    <span class="tag is-info is-light is-rounded">
                                        ${product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getDisplayName() : "Unidad"} 
                                        (${product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getSymbol() : "u"})
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">Stock Mínimo</th>
                                <td class="has-text-danger has-text-weight-bold">${product.getMinStockThreshold()}</td>
                            </tr>
                            <tr>
                                <th class="has-text-grey">Stock Deseado</th>
                                <td class="has-text-primary has-text-weight-bold">${product.getTargetStockQuantity()}</td>
                            </tr>
                        </tbody>
                    </table>

                    @if(product.getDescription() != null && !product.getDescription().isBlank())
                        <div class="mt-4">
                            <h3 class="is-size-7 has-text-weight-bold has-text-grey">DESCRIPCIÓN</h3>
                            <p class="is-size-6 mt-1">${product.getDescription()}</p>
                        </div>
                    @endif
                </div>
            </div>

            <!-- Existencia en Tiempo Real -->
            <div class="column is-8">
                <div class="box">
                    <h2 class="title is-6 mb-3">Existencia Física en Casa</h2>
                    @if(stockOverview != null)
                        <div class="columns is-multiline mb-4">
                            <div class="column is-6">
                                <div class="nano-stat-card">
                                    <span class="stat-label">Stock Total</span>
                                    <div class="stat-value has-text-primary">
                                        ${stockOverview.getTotalQuantity()} 
                                        <span class="is-size-6 has-text-grey">${product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getSymbol() : "u"}</span>
                                    </div>
                                    <span class="is-size-7 has-text-grey">en ${stockOverview.getActiveBatches() != null ? stockOverview.getActiveBatches().size() : 0} lotes</span>
                                </div>
                            </div>
                            <div class="column is-6">
                                <div class="nano-stat-card">
                                    <span class="stat-label">Estado de Reposición</span>
                                    <div class="stat-value">
                                        @if(stockOverview.isLowStock())
                                            <span class="has-text-danger">Crítico</span>
                                        @else
                                            <span class="has-text-success">Óptimo</span>
                                        @endif
                                    </div>
                                    <span class="is-size-7 has-text-grey">Meta: ${product.getTargetStockQuantity()}</span>
                                </div>
                            </div>
                        </div>

                        <!-- Lotes Físicos Asociados -->
                        <h3 class="subtitle is-6 mb-2">Lotes y Caducidades (FEFO)</h3>
                        @if(stockOverview.getActiveBatches() == null || stockOverview.getActiveBatches().isEmpty())
                            <p class="has-text-grey is-size-6">No hay lotes físicos registrados en inventario para este producto.</p>
                        @else
                            <table class="table is-fullwidth is-hoverable is-narrow">
                                <thead>
                                    <tr>
                                        <th>Ubicación</th>
                                        <th>Cantidad</th>
                                        <th>Vencimiento</th>
                                        <th>Empaque</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    @for(StockBatchResponse batch : stockOverview.getActiveBatches())
                                        <tr>
                                            <td>${batch.getLocationName()}</td>
                                            <td class="has-text-weight-semibold">${batch.getQuantity()}</td>
                                            <td>
                                                @if(batch.getExpirationDate() != null)
                                                    <code>${batch.getExpirationDate().toString()}</code>
                                                @else
                                                    <span class="has-text-grey">Sin fecha</span>
                                                @endif
                                            </td>
                                            <td>
                                                <span class="tag is-rounded is-small ${batch.getSealedStatus() != null && batch.getSealedStatus().name().equals("OPENED") ? "is-warning" : "is-success is-light"}">
                                                    ${batch.getSealedStatus() != null ? batch.getSealedStatus().name() : "SEALED"}
                                                </span>
                                            </td>
                                        </tr>
                                    @endfor
                                </tbody>
                            </table>
                        @endif
                    @else
                        <p class="has-text-grey">Información de stock no disponible de inmediato.</p>
                    @endif
                </div>
            </div>
        </div>
    `
)
