@import java.util.List
@import cl.diego.NanoStock.inventory.dto.InventoryMovementResponse
@import cl.diego.NanoStock.catalog.dto.ProductResponse

@param List<InventoryMovementResponse> movements
@param List<ProductResponse> allProducts
@param Long selectedProduct = null

@template.layouts.app(
    title = "Bitácora de Movimientos",
    activeSection = "movements",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Bitácora de Auditoría de Movimientos</h1>
                    <p class="subtitle is-6 has-text-grey">Registro inmutable de entradas, consumos, mermas, traslados y ajustes de stock.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="/inventory" class="button is-primary is-light">← Volver al Inventario</a>
            </div>
        </div>

        <!-- Filtro por Producto -->
        <div class="box p-3 mb-4">
            <form action="/inventory/movements" method="get">
                <div class="field is-grouped">
                    <div class="control is-expanded">
                        <div class="select is-fullwidth">
                            <select name="productId" onchange="this.form.submit()">
                                <option value="">Todos los Productos</option>
                                @for(ProductResponse p : allProducts)
                                    <option value="${p.getId()}" selected="${selectedProduct != null && selectedProduct.equals(p.getId())}">
                                        ${p.getName()} (${p.getBarCode()})
                                    </option>
                                @endfor
                            </select>
                        </div>
                    </div>
                    <div class="control">
                        <button type="submit" class="button is-info">Filtrar</button>
                        @if(selectedProduct != null)
                            <a href="/inventory/movements" class="button is-light">Ver Todos</a>
                        @endif
                    </div>
                </div>
            </form>
        </div>

        <!-- Tabla de Movimientos -->
        <div class="box p-0" style="overflow-x: auto;">
            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr class="has-background-light">
                        <th>Fecha / Hora</th>
                        <th>Producto</th>
                        <th>Tipo de Movimiento</th>
                        <th class="has-text-centered">Cantidad</th>
                        <th>Ubicación</th>
                        <th>Usuario</th>
                        <th>Motivo / Notas</th>
                    </tr>
                </thead>
                <tbody>
                    @if(movements.isEmpty())
                        <tr>
                            <td colspan="7" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">📜</span>
                                <p class="mt-2">No se han registrado movimientos de inventario aún.</p>
                            </td>
                        </tr>
                    @else
                        @for(InventoryMovementResponse m : movements)
                            <tr>
                                <td class="is-size-7 has-text-grey">
                                    ${m.getCreatedAt() != null ? m.getCreatedAt().toString().replace("T", " ").substring(0, 16) : ""}
                                </td>
                                <td>
                                    <span class="has-text-weight-semibold">${m.getProductName()}</span>
                                </td>
                                <td>
                                    @if(m.getMovementType() != null)
                                        @if(m.getMovementType().name().equals("ENTRY"))
                                            <span class="tag is-success is-light is-rounded">➕ Entrada</span>
                                        @elseif(m.getMovementType().name().equals("CONSUMPTION"))
                                            <span class="tag is-info is-light is-rounded">🍽️ Consumo</span>
                                        @elseif(m.getMovementType().name().equals("WASTE"))
                                            <span class="tag is-danger is-light is-rounded">🗑️ Merma</span>
                                        @elseif(m.getMovementType().name().equals("TRANSFER"))
                                            <span class="tag is-link is-light is-rounded">🔀 Traslado</span>
                                        @else
                                            <span class="tag is-warning is-light is-rounded">⚖️ Ajuste</span>
                                        @endif
                                    @endif
                                </td>
                                <td class="has-text-centered has-text-weight-bold">
                                    ${m.getQuantity()}
                                </td>
                                <td>
                                    <span class="tag is-light">${m.getLocationName() != null ? m.getLocationName() : "General"}</span>
                                </td>
                                <td class="is-size-7">
                                    ${m.getUsername() != null ? m.getUsername() : "Sistema"}
                                </td>
                                <td class="is-size-7">
                                    @if(m.getWasteReason() != null && !m.getWasteReason().isBlank())
                                        <span class="has-text-danger has-text-weight-semibold">${m.getWasteReason()}</span>: 
                                    @endif
                                    ${m.getNotes() != null ? m.getNotes() : "-"}
                                </td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>
    `
)
