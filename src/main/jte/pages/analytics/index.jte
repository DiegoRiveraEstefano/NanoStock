@import java.util.List
@import cl.diego.NanoStock.analytics.dto.ConsumptionAutonomyResponse
@import cl.diego.NanoStock.analytics.dto.WasteReportResponse

@param List<ConsumptionAutonomyResponse> autonomies
@param WasteReportResponse wasteReport

@template.layouts.app(
    title = "Analíticas Domésticas",
    activeSection = "analytics",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Analíticas & Finanzas del Hogar</h1>
                    <p class="subtitle is-6 has-text-grey">Proyección de autonomía, ritmos de consumo y costo financiero de desperdicios.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="/data/export" class="button is-info is-light">
                    <span>📤</span> <span class="ml-1">Exportar Reportes</span>
                </a>
            </div>
        </div>

        <!-- KPIs Financieros y de Merma -->
        <div class="columns is-multiline mb-4">
            <div class="column is-4">
                <div class="nano-stat-card">
                    <span class="stat-label">Pérdida por Desperdicio (Merma)</span>
                    <div class="stat-value has-text-danger">
                        $${wasteReport != null && wasteReport.getTotalEstimatedFinancialLoss() != null ? wasteReport.getTotalEstimatedFinancialLoss().toString() : "0.00"}
                    </div>
                    <span class="is-size-7 has-text-grey">
                        ${wasteReport != null ? wasteReport.getTotalIncidents() : 0} incidentes registrados
                    </span>
                </div>
            </div>
            <div class="column is-4">
                <div class="nano-stat-card">
                    <span class="stat-label">Cantidad Física Desechada</span>
                    <div class="stat-value has-text-warning">
                        ${wasteReport != null && wasteReport.getTotalQuantityWasted() != null ? wasteReport.getTotalQuantityWasted() : 0.0}
                    </div>
                    <span class="is-size-7 has-text-grey">Unidades / Kilos perdidos</span>
                </div>
            </div>
            <div class="column is-4">
                <div class="nano-stat-card">
                    <span class="stat-label">Artículos en Riesgo de Quiebre</span>
                    <div class="stat-value has-text-info">
                        ${autonomies.stream().filter(ConsumptionAutonomyResponse::isRiskOfStockout).count()}
                    </div>
                    <span class="is-size-7 has-text-grey">Autonomía estimada &lt; 3 días</span>
                </div>
            </div>
        </div>

        <!-- Tabla: Autonomía y Proyección de Stockout -->
        <div class="box p-0 mb-5" style="overflow-x: auto;">
            <div class="p-3 has-background-light is-flex is-justify-content-space-between is-align-items-center">
                <h2 class="title is-6 mb-0">Ritmo de Consumo y Días de Autonomía</h2>
                <span class="tag is-info is-light">${autonomies.size()} productos monitoreados</span>
            </div>
            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Stock Actual</th>
                        <th class="has-text-centered">Consumo Diario Prom.</th>
                        <th class="has-text-centered">Días de Autonomía</th>
                        <th class="has-text-centered">Fecha Proyectada Quiebre</th>
                        <th class="has-text-centered">Estado</th>
                    </tr>
                </thead>
                <tbody>
                    @if(autonomies.isEmpty())
                        <tr>
                            <td colspan="6" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">📈</span>
                                <p class="mt-2">No hay suficientes registros de consumo para proyectar ritmos de autonomía.</p>
                            </td>
                        </tr>
                    @else
                        @for(ConsumptionAutonomyResponse aut : autonomies)
                            <tr>
                                <td class="has-text-weight-bold">${aut.getProductName()}</td>
                                <td>${aut.getCurrentStock()} ${aut.getUnitOfMeasure() != null ? aut.getUnitOfMeasure().getSymbol() : "u"}</td>
                                <td class="has-text-centered">${String.format("%.2f", aut.getAverageDailyConsumption())} / día</td>
                                <td class="has-text-centered has-text-weight-bold is-size-5 ${aut.isRiskOfStockout() ? "has-text-danger" : "has-text-primary"}">
                                    ${aut.getEstimatedDaysOfAutonomy() != null ? String.format("%.1f", aut.getEstimatedDaysOfAutonomy()) : "∞"} días
                                </td>
                                <td class="has-text-centered">
                                    ${aut.getProjectedStockoutDate() != null ? aut.getProjectedStockoutDate().toString() : "Indefinida"}
                                </td>
                                <td class="has-text-centered">
                                    @if(aut.isRiskOfStockout())
                                        <span class="tag is-danger is-light is-rounded">⚠️ Riesgo Inminente</span>
                                    @else
                                        <span class="tag is-success is-light is-rounded">Abastecido</span>
                                    @endif
                                </td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>

        <!-- Desglose de Desperdicio por Categoría -->
        @if(wasteReport != null && wasteReport.getLossByCategory() != null && !wasteReport.getLossByCategory().isEmpty())
            <div class="box">
                <h2 class="title is-6 mb-3">Distribución de Pérdidas por Categoría</h2>
                <div class="columns is-multiline">
                    @for(var entry : wasteReport.getLossByCategory().entrySet())
                        <div class="column is-4">
                            <div class="notification is-light p-3">
                                <p class="is-size-7 has-text-grey has-text-weight-bold uppercase">${entry.getKey()}</p>
                                <p class="is-size-4 has-text-weight-bold has-text-danger">$${entry.getValue()}</p>
                            </div>
                        </div>
                    @endfor
                </div>
            </div>
        @endif
    `
)
