@import java.util.List
@import cl.diego.NanoStock.inventory.dto.ProductStockOverview
@import cl.diego.NanoStock.inventory.dto.LocationResponse
@import cl.diego.NanoStock.catalog.dto.ProductResponse
@import cl.diego.NanoStock.inventory.dto.StockBatchResponse

@param List<ProductStockOverview> stockOverviews
@param List<LocationResponse> locations
@param List<ProductResponse> allProducts
@param List<StockBatchResponse> expiringBatches
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Inventario & Control FEFO",
    activeSection = "inventory",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Inventario Físico & Control FEFO</h1>
                    <p class="subtitle is-6 has-text-grey">Existencias en el hogar, control de lotes y fechas de caducidad.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <button class="button is-primary" onclick="document.getElementById('modal-entry').open()">
                        <span>➕</span> <span class="ml-1">Ingresar Lote de Stock</span>
                    </button>
                    <button class="button is-warning is-light" onclick="document.getElementById('modal-consume').open()">
                        <span>🍽️</span> <span class="ml-1">Registrar Consumo</span>
                    </button>
                    <button class="button is-danger is-light" onclick="document.getElementById('modal-waste').open()">
                        <span>🗑️</span> <span class="ml-1">Dar de Baja (Merma)</span>
                    </button>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "✅")
        @endif

        <!-- Alertas de Caducidad Próxima (FEFO) -->
        @if(!expiringBatches.isEmpty())
            <div class="message is-warning mb-5">
                <div class="message-header">
                    <p>⚠️ Lotes que vencen en los próximos 7 días (Prioridad FEFO)</p>
                </div>
                <div class="message-body p-0">
                    <div style="overflow-x: auto;">
                        <table class="table is-fullwidth is-narrow is-striped mb-0">
                            <thead>
                                <tr>
                                    <th>Producto</th>
                                    <th>Ubicación</th>
                                    <th>Cantidad</th>
                                    <th>Fecha Caducidad</th>
                                    <th>Días Restantes</th>
                                    <th>Estado Empaque</th>
                                </tr>
                            </thead>
                            <tbody>
                                @for(StockBatchResponse exp : expiringBatches)
                                    <tr>
                                        <td class="has-text-weight-bold">${exp.getProductName()}</td>
                                        <td><span class="tag is-light is-info">${exp.getLocationName()}</span></td>
                                        <td>${exp.getQuantity()}</td>
                                        <td><code>${exp.getExpirationDate() != null ? exp.getExpirationDate().toString() : "N/A"}</code></td>
                                        <td>
                                            @if(exp.getDaysUntilExpiration() != null && exp.getDaysUntilExpiration() <= 2)
                                                <span class="badge-fefo-critical">${exp.getDaysUntilExpiration()} días (Urgente)</span>
                                            @else
                                                <span class="badge-fefo-warning">${exp.getDaysUntilExpiration()} días</span>
                                            @endif
                                        </td>
                                        <td>
                                            <span class="tag is-small ${exp.getSealedStatus() != null && exp.getSealedStatus().name().equals("OPENED") ? "is-warning" : "is-success is-light"}">
                                                ${exp.getSealedStatus() != null ? exp.getSealedStatus().name() : "SEALED"}
                                            </span>
                                        </td>
                                    </tr>
                                @endfor
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        @endif

        <!-- Tabla de Existencias Generales -->
        <div class="box p-0 mb-5" style="overflow-x: auto;">
            <div class="p-3 has-background-light is-flex is-justify-content-space-between is-align-items-center">
                <h2 class="title is-6 mb-0">Resumen de Existencias por Producto</h2>
                <span class="tag is-info is-light">${stockOverviews.size()} artículos con stock</span>
            </div>
            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Código / SKU</th>
                        <th class="has-text-centered">UOM</th>
                        <th class="has-text-centered">Stock Total</th>
                        <th class="has-text-centered">Mín / Deseado</th>
                        <th class="has-text-centered">Lotes Físicos</th>
                        <th class="has-text-centered">Estado Reposición</th>
                        <th class="has-text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    @if(stockOverviews.isEmpty())
                        <tr>
                            <td colspan="8" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">🏷️</span>
                                <p class="mt-2">No hay existencias registradas en este momento.</p>
                                <button class="button is-small is-primary mt-3" onclick="document.getElementById('modal-entry').open()">
                                    Ingresar Primer Lote
                                </button>
                            </td>
                        </tr>
                    @else
                        @for(ProductStockOverview item : stockOverviews)
                            <tr>
                                <td>
                                    <a href="/catalog/${item.getProductId()}" class="has-text-weight-bold has-text-dark">
                                        ${item.getProductName()}
                                    </a>
                                </td>
                                <td>
                                    <div><code class="is-size-7">${item.getBarCode()}</code></div>
                                </td>
                                <td class="has-text-centered">
                                    <span class="tag is-small is-rounded">
                                        ${item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getSymbol() : "u"}
                                    </span>
                                </td>
                                <td class="has-text-centered has-text-weight-bold is-size-5 has-text-primary">
                                    ${item.getTotalQuantity()}
                                </td>
                                <td class="has-text-centered">
                                    <span class="has-text-danger">${item.getMinStockThreshold()}</span> / 
                                    <span class="has-text-grey">${item.getTargetStockQuantity()}</span>
                                </td>
                                <td class="has-text-centered">
                                    <span class="tag is-info is-light is-rounded">
                                        ${item.getActiveBatches() != null ? item.getActiveBatches().size() : 0} lotes
                                    </span>
                                </td>
                                <td class="has-text-centered">
                                    @if(item.isLowStock())
                                        <span class="tag is-danger is-light is-rounded">Bajo Umbral</span>
                                    @else
                                        <span class="tag is-success is-light is-rounded">Normal</span>
                                    @endif
                                </td>
                                <td class="has-text-right">
                                    <div class="buttons are-small is-right">
                                        <a href="/catalog/${item.getProductId()}" class="button is-light" title="Ver Lotes">👁️</a>
                                    </div>
                                </td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>

        <!-- MODAL: Ingresar Lote de Stock -->
        <nano-modal id="modal-entry" title="Ingresar Nuevo Lote Físico">
            <form id="form-entry" action="/inventory/stock/entry" method="post">
                <div class="field">
                    <label class="label">Producto <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="productId" required>
                                <option value="">-- Selecciona un Producto --</option>
                                @for(ProductResponse pr : allProducts)
                                    <option value="${pr.getId()}">${pr.getName()} (${pr.getBarCode()})</option>
                                @endfor
                            </select>
                        </div>
                    </div>
                </div>

                <div class="columns is-multiline">
                    <div class="column is-6">
                        <div class="field">
                            <label class="label">Ubicación de Almacenamiento <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="locationId" required>
                                        @for(LocationResponse loc : locations)
                                            <option value="${loc.getId()}">${loc.getName()} (${loc.getStorageType()})</option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="column is-6">
                        <div class="field">
                            <label class="label">Cantidad <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <input class="input" type="number" step="0.1" min="0.1" name="quantity" placeholder="1.0" required>
                            </div>
                        </div>
                    </div>

                    <div class="column is-6">
                        <div class="field">
                            <label class="label">Fecha de Caducidad (FEFO)</label>
                            <div class="control">
                                <input class="input" type="date" name="expirationDate">
                            </div>
                        </div>
                    </div>

                    <div class="column is-6">
                        <div class="field">
                            <label class="label">Estado del Empaque</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="sealedStatus">
                                        <option value="SEALED">Cerrado (Sellado)</option>
                                        <option value="OPENED">Abierto</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="column is-12">
                        <div class="field">
                            <label class="label">Notas / Lote comercial</label>
                            <div class="control">
                                <input class="input" type="text" name="notes" placeholder="Ej. Lote L-204, compra de almacén">
                            </div>
                        </div>
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-entry" class="button is-primary">Registrar Ingreso</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>

        <!-- MODAL: Consumo Rápido FEFO -->
        <nano-modal id="modal-consume" title="Registrar Consumo de Despensa (Deducción FEFO)">
            <form id="form-consume" action="/inventory/stock/consume" method="post">
                <div class="field">
                    <label class="label">Producto a Consumir <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="productId" required>
                                <option value="">-- Selecciona el Producto Consumido --</option>
                                @for(ProductStockOverview item : stockOverviews)
                                    <option value="${item.getProductId()}">
                                        ${item.getProductName()} (Disponible: ${item.getTotalQuantity()} ${item.getUnitOfMeasure() != null ? item.getUnitOfMeasure().getSymbol() : "u"})
                                    </option>
                                @endfor
                            </select>
                        </div>
                    </div>
                    <p class="help">El algoritmo FEFO descontará automáticamente del lote más próximo a caducar.</p>
                </div>

                <div class="field">
                    <label class="label">Cantidad Consumida <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <input class="input" type="number" step="0.1" min="0.1" name="quantity" placeholder="1.0" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Notas (Opcional)</label>
                    <div class="control">
                        <input class="input" type="text" name="notes" placeholder="Ej. Preparación almuerzo familiar">
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-consume" class="button is-warning">Deducir FEFO</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>

        <!-- MODAL: Registrar Merma / Desperdicio -->
        <nano-modal id="modal-waste" title="Dar de Baja por Merma o Desperdicio">
            <form id="form-waste" action="/inventory/stock/waste" method="post">
                <div class="field">
                    <label class="label">Lote Físico Afectado <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="batchId" required>
                                <option value="">-- Selecciona el Lote con Merma --</option>
                                @for(ProductStockOverview ov : stockOverviews)
                                    @if(ov.getActiveBatches() != null)
                                        @for(StockBatchResponse b : ov.getActiveBatches())
                                            <option value="${b.getId()}">
                                                ${b.getProductName()} - ${b.getLocationName()} (${b.getQuantity()} disp. | Vence: ${b.getExpirationDate() != null ? b.getExpirationDate().toString() : "S/F"})
                                            </option>
                                        @endfor
                                    @endif
                                @endfor
                            </select>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Cantidad a Descartar <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <input class="input" type="number" step="0.1" min="0.1" name="quantity" placeholder="1.0" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Causa del Desperdicio <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="wasteReason" required>
                                <option value="Vencido">Alimento Vencido / Caducado</option>
                                <option value="Descompuesto">Descompuesto / Hongos / Mal estado</option>
                                <option value="Envase Dañado">Envase Roto o Dañado</option>
                                <option value="Otro">Otro motivo</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Observaciones Adicionales</label>
                    <div class="control">
                        <input class="input" type="text" name="notes" placeholder="Detalles de la pérdida...">
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-waste" class="button is-danger">Confirmar Merma</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>
    `
)
