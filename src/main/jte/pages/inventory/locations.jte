@import java.util.List
@import cl.diego.NanoStock.inventory.dto.LocationResponse
@import cl.diego.NanoStock.inventory.dto.StockBatchResponse
@import cl.diego.NanoStock.inventory.model.StorageType

@param List<LocationResponse> locations
@param StorageType[] storageTypes
@param LocationResponse selectedLocation = null
@param List<StockBatchResponse> locationBatches = null
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Ubicaciones de Almacenamiento",
    activeSection = "locations",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Ubicaciones de Almacenamiento</h1>
                    <p class="subtitle is-6 has-text-grey">Despensas, refrigeradores, congeladores y espacios físicos del hogar.</p>
                </div>
            </div>
            <div class="level-right">
                <div class="buttons">
                    <a href="/inventory" class="button is-light">
                        <span>🏷️</span> <span class="ml-1">Volver a Inventario</span>
                    </a>
                    <button class="button is-primary" onclick="document.getElementById('modal-add-location').open()">
                        <span>➕</span> <span class="ml-1">Nueva Ubicación</span>
                    </button>
                </div>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = flashType.equals("is-danger") ? "⚠️" : "✅")
        @endif

        <!-- Tabla de Ubicaciones -->
        <div class="box p-0 mb-5" style="overflow-x: auto;">
            <div class="p-3 has-background-light is-flex is-justify-content-space-between is-align-items-center">
                <h2 class="title is-6 mb-0">Espacios Registrados</h2>
                <span class="tag is-info is-light">${locations.size()} ubicaciones</span>
            </div>
            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Tipo de Almacenamiento</th>
                        <th>Descripción</th>
                        <th class="has-text-centered">Lotes Activos</th>
                        <th class="has-text-centered">Unidades / Stock</th>
                        <th class="has-text-right">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    @if(locations.isEmpty())
                        <tr>
                            <td colspan="6" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">📍</span>
                                <p class="mt-2">No hay ubicaciones de almacenamiento registradas.</p>
                                <button class="button is-small is-primary mt-3" onclick="document.getElementById('modal-add-location').open()">
                                    Crear Primera Ubicación
                                </button>
                            </td>
                        </tr>
                    @else
                        @for(LocationResponse loc : locations)
                            <tr class="${selectedLocation != null && selectedLocation.getId().equals(loc.getId()) ? "is-selected" : ""}">
                                <td class="has-text-weight-bold">${loc.getName()}</td>
                                <td>
                                    @if(loc.getStorageType() == StorageType.REFRIGERATED)
                                        <span class="tag is-info is-light is-rounded">❄️ ${loc.getStorageType().getDisplayName()}</span>
                                    @elseif(loc.getStorageType() == StorageType.FROZEN)
                                        <span class="tag is-link is-light is-rounded">🧊 ${loc.getStorageType().getDisplayName()}</span>
                                    @elseif(loc.getStorageType() == StorageType.AMBIENT)
                                        <span class="tag is-warning is-light is-rounded">🧺 ${loc.getStorageType().getDisplayName()}</span>
                                    @else
                                        <span class="tag is-light is-rounded">📦 ${loc.getStorageType() != null ? loc.getStorageType().getDisplayName() : "Otro"}</span>
                                    @endif
                                </td>
                                <td class="is-size-7 has-text-grey">
                                    ${loc.getDescription() != null && !loc.getDescription().isBlank() ? loc.getDescription() : "-"}
                                </td>
                                <td class="has-text-centered">
                                    <span class="tag is-rounded ${loc.getActiveBatchesCount() > 0 ? "is-primary is-light" : "is-light"}">
                                        ${loc.getActiveBatchesCount()} lotes
                                    </span>
                                </td>
                                <td class="has-text-centered has-text-weight-semibold">
                                    ${loc.getTotalQuantity()}
                                </td>
                                <td class="has-text-right">
                                    <div class="buttons are-small is-right">
                                        <a href="/inventory/locations?locationId=${loc.getId()}" class="button is-light" title="Ver lotes en esta ubicación">
                                            👁️
                                        </a>
                                        <button type="button"
                                                class="button is-info is-light btn-edit-location"
                                                data-id="${loc.getId()}"
                                                data-name="${loc.getName()}"
                                                data-storage-type="${loc.getStorageType() != null ? loc.getStorageType().name() : "AMBIENT"}"
                                                data-description="${loc.getDescription() != null ? loc.getDescription() : ""}"
                                                title="Editar ubicación">
                                            ✏️
                                        </button>
                                        <form action="/inventory/locations/${loc.getId()}/delete" method="post" style="display: inline;"
                                              onsubmit="return confirm('¿Eliminar la ubicación \'${loc.getName()}\'?');">
                                            <button type="submit"
                                                    class="button is-danger is-light"
                                                    title="${loc.getActiveBatchesCount() > 0 ? "No se puede eliminar: tiene lotes activos" : "Eliminar ubicación"}">
                                                🗑️
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>

        <!-- Detalle de Lotes de la Ubicación Seleccionada -->
        @if(selectedLocation != null)
            <div class="box p-0 mb-5" style="overflow-x: auto;">
                <div class="p-3 has-background-light is-flex is-justify-content-space-between is-align-items-center">
                    <div>
                        <h2 class="title is-6 mb-0">Stock Almacenado en: ${selectedLocation.getName()}</h2>
                        <p class="is-size-7 has-text-grey">
                            ${selectedLocation.getStorageType() != null ? selectedLocation.getStorageType().getDisplayName() : ""}
                        </p>
                    </div>
                    <a href="/inventory/locations" class="button is-small is-light">Cerrar Detalle</a>
                </div>
                <table class="table is-striped is-hoverable is-fullwidth mb-0">
                    <thead>
                        <tr>
                            <th>Producto</th>
                            <th>Código / SKU</th>
                            <th class="has-text-centered">Cantidad</th>
                            <th>Caducidad</th>
                            <th>Empaque</th>
                            <th>Lote</th>
                        </tr>
                    </thead>
                    <tbody>
                        @if(locationBatches == null || locationBatches.isEmpty())
                            <tr>
                                <td colspan="6" class="has-text-centered p-4 has-text-grey">
                                    Esta ubicación no contiene lotes con stock activo en este momento.
                                </td>
                            </tr>
                        @else
                            @for(StockBatchResponse batch : locationBatches)
                                <tr>
                                    <td>
                                        <a href="/catalog/${batch.getProductId()}" class="has-text-weight-bold has-text-dark">
                                            ${batch.getProductName()}
                                        </a>
                                    </td>
                                    <td><code class="is-size-7">${batch.getProductBarCode()}</code></td>
                                    <td class="has-text-centered has-text-weight-bold">${batch.getQuantity()}</td>
                                    <td>
                                        @if(batch.getExpirationDate() != null)
                                            <code>${batch.getExpirationDate().toString()}</code>
                                        @else
                                            <span class="has-text-grey">Sin fecha</span>
                                        @endif
                                    </td>
                                    <td>
                                        <span class="tag is-small ${batch.getSealedStatus() != null && batch.getSealedStatus().name().equals("OPEN") ? "is-warning" : "is-success is-light"}">
                                            ${batch.getSealedStatus() != null ? batch.getSealedStatus().name() : "SEALED"}
                                        </span>
                                    </td>
                                    <td class="is-size-7 has-text-grey">${batch.getBatchCode() != null ? batch.getBatchCode() : "-"}</td>
                                </tr>
                            @endfor
                        @endif
                    </tbody>
                </table>
            </div>
        @endif

        <!-- MODAL: Nueva Ubicación -->
        <nano-modal id="modal-add-location" title="Registrar Nueva Ubicación">
            <form id="form-add-location" action="/inventory/locations" method="post">
                <div class="field">
                    <label class="label">Nombre de la Ubicación <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <input class="input" type="text" name="name" placeholder="Ej. Despensa Principal, Refrigerador Cocina, Congelador" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Tipo de Almacenamiento <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="storageType" required>
                                @for(StorageType st : storageTypes)
                                    <option value="${st.name()}">${st.getDisplayName()}</option>
                                @endfor
                            </select>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Descripción</label>
                    <div class="control">
                        <textarea class="textarea has-fixed-size" rows="2" name="description" placeholder="Ej. Repisa superior derecha o puerta del refrigerador"></textarea>
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-add-location" class="button is-primary">Guardar Ubicación</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>

        <!-- MODAL: Editar Ubicación -->
        <nano-modal id="modal-edit-location" title="Editar Ubicación">
            <form id="form-edit-location" action="" method="post">
                <div class="field">
                    <label class="label">Nombre de la Ubicación <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <input id="edit-location-name" class="input" type="text" name="name" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Tipo de Almacenamiento <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select id="edit-location-storage-type" name="storageType" required>
                                @for(StorageType st : storageTypes)
                                    <option value="${st.name()}">${st.getDisplayName()}</option>
                                @endfor
                            </select>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Descripción</label>
                    <div class="control">
                        <textarea id="edit-location-description" class="textarea has-fixed-size" rows="2" name="description"></textarea>
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-edit-location" class="button is-info">Guardar Cambios</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>
    `,
    scripts = @`
        <script>
            document.addEventListener('DOMContentLoaded', () => {
                const editModal = document.getElementById('modal-edit-location');
                const editForm = document.getElementById('form-edit-location');
                const editName = document.getElementById('edit-location-name');
                const editStorageType = document.getElementById('edit-location-storage-type');
                const editDescription = document.getElementById('edit-location-description');

                document.querySelectorAll('.btn-edit-location').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const id = btn.dataset.id;
                        editForm.action = '/inventory/locations/' + id + '/update';
                        editName.value = btn.dataset.name || '';
                        editStorageType.value = btn.dataset.storageType || 'AMBIENT';
                        editDescription.value = btn.dataset.description || '';
                        editModal.open();
                    });
                });
            });
        </script>
    `
)
