@import cl.diego.NanoStock.dataimport.dto.ImportResultDto

@param ImportResultDto importResult = null
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Importación Masiva de Datos",
    activeSection = "data-import",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Importación Masiva de Catálogo e Inventario</h1>
                    <p class="subtitle is-6 has-text-grey">Carga masiva resiliente de productos o existencias en Excel (.xlsx), CSV o JSON.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <a href="/api/import/templates/products/excel" class="button is-small is-light" download>
                        <span>📥</span> <span class="ml-1">Plantilla Productos (.xlsx)</span>
                    </a>
                    <a href="/api/import/templates/inventory/excel" class="button is-small is-light" download>
                        <span>📥</span> <span class="ml-1">Plantilla Inventario (.xlsx)</span>
                    </a>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "📦")
        @endif

        <!-- Formulario de Subida -->
        <div class="box mb-5">
            <form action="/data/import" method="post" enctype="multipart/form-data">
                <div class="columns is-multiline">
                    <!-- Tipo de Importación -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Tipo de Entidad a Importar <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="targetType">
                                        <option value="PRODUCTS">Catálogo Maestro de Productos</option>
                                        <option value="INVENTORY">Existencias Físicas e Inventario</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Estrategia de Conflicto -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Resolución de Conflictos</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="conflictStrategy">
                                        <option value="UPDATE">Actualizar existentes (UPDATE)</option>
                                        <option value="SKIP">Ignorar duplicados (SKIP)</option>
                                        <option value="FAIL">Detener ante errores (FAIL)</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Modo Dry Run -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Modo de Ejecución</label>
                            <div class="control mt-2">
                                <label class="checkbox">
                                    <input type="checkbox" name="dryRun" value="true">
                                    <span class="ml-2 has-text-weight-bold">Simulación previa (Dry-Run)</span>
                                </label>
                            </div>
                            <p class="help">Valida las filas sin aplicar cambios a la base de datos.</p>
                        </div>
                    </div>

                    <!-- Archivo a Subir -->
                    <div class="column is-12">
                        <div class="file has-name is-fullwidth">
                            <label class="file-label">
                                <input class="file-input" type="file" name="file" id="importFileInput" required onchange="document.getElementById('fileNameDisplay').textContent = this.files[0] ? this.files[0].name : 'Ningún archivo seleccionado'">
                                <span class="file-cta">
                                    <span class="file-icon">📁</span>
                                    <span class="file-label">Elegir archivo...</span>
                                </span>
                                <span class="file-name" id="fileNameDisplay">
                                    Ningún archivo seleccionado (.xlsx, .csv, .json)
                                </span>
                            </label>
                        </div>
                    </div>
                </div>

                <div class="field is-grouped mt-4">
                    <div class="control">
                        <button type="submit" class="button is-primary">
                            <span>🚀</span> <span class="ml-1">Comenzar Importación</span>
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <!-- Resultado de la Importación -->
        @if(importResult != null)
            <div class="box ${importResult.isSuccessful() ? "has-background-white" : "has-background-danger-light"}">
                <div class="level mb-3">
                    <div class="level-left">
                        <h2 class="title is-5 mb-0">Resultado de la Operación</h2>
                        @if(importResult.isDryRun())
                            <span class="tag is-warning ml-2 is-rounded">Simulación Dry-Run</span>
                        @endif
                    </div>
                    <div class="level-right">
                        <span class="tag is-medium ${importResult.isSuccessful() ? "is-success" : "is-danger"}">
                            ${importResult.isSuccessful() ? "Completado con Éxito" : "Con Observaciones"}
                        </span>
                    </div>
                </div>

                <div class="columns is-mobile has-text-centered mb-4">
                    <div class="column">
                        <p class="heading">Filas Totales</p>
                        <p class="title is-4">${importResult.getTotalRows()}</p>
                    </div>
                    <div class="column">
                        <p class="heading has-text-success">Procesadas</p>
                        <p class="title is-4 has-text-success">${importResult.getSuccessCount()}</p>
                    </div>
                    <div class="column">
                        <p class="heading has-text-warning">Omitidas</p>
                        <p class="title is-4 has-text-warning">${importResult.getSkippedCount()}</p>
                    </div>
                    <div class="column">
                        <p class="heading has-text-danger">Errores</p>
                        <p class="title is-4 has-text-danger">${importResult.getErrorCount()}</p>
                    </div>
                </div>

                <!-- Detalle de Errores -->
                @if(importResult.getErrors() != null && !importResult.getErrors().isEmpty())
                    <h3 class="subtitle is-6 has-text-danger mb-2">Incidencias por Fila:</h3>
                    <div class="table-container">
                        <table class="table is-fullwidth is-narrow is-striped">
                            <thead>
                                <tr>
                                    <th>Fila #</th>
                                    <th>Campo</th>
                                    <th>Mensaje de Error</th>
                                </tr>
                            </thead>
                            <tbody>
                                @for(var err : importResult.getErrors())
                                    <tr>
                                        <td><strong>Fila ${err.getRowNumber()}</strong></td>
                                        <td><code>${err.getField() != null ? err.getField() : "-"}</code></td>
                                        <td class="has-text-danger">${err.getErrorMessage()}</td>
                                    </tr>
                                @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </div>
        @endif
    `
)
