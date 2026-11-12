@import java.util.List
@import cl.diego.NanoStock.purchases.dto.StoreResponse
@import cl.diego.NanoStock.purchases.model.StoreType

@param List<StoreResponse> stores
@param String flashMessage = null
@param String flashType = "is-success"

@template.layouts.app(
    title = "Comercios Habituales",
    activeSection = "stores",
    content = @`
        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">Comercios y Proveedores Habituales</h1>
                    <p class="subtitle is-6 has-text-grey">Supermercados, ferias y tiendas donde adquiere productos el hogar.</p>
                </div>
            </div>
            <div class="level-right">
                <button class="button is-primary" onclick="document.getElementById('modal-add-store').open()">
                    <span>➕</span> <span class="ml-1">Nuevo Comercio</span>
                </button>
            </div>
        </div>

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = "✅")
        @endif

        <div class="box p-0" style="overflow-x: auto;">
            <table class="table is-striped is-hoverable is-fullwidth mb-0">
                <thead>
                    <tr class="has-background-light">
                        <th>Nombre</th>
                        <th>Sucursal / Descripción</th>
                        <th>Tipo de Comercio</th>
                        <th>Dirección</th>
                    </tr>
                </thead>
                <tbody>
                    @if(stores.isEmpty())
                        <tr>
                            <td colspan="4" class="has-text-centered p-5 has-text-grey">
                                <span class="is-size-3">🏪</span>
                                <p class="mt-2">No hay comercios registrados.</p>
                            </td>
                        </tr>
                    @else
                        @for(StoreResponse s : stores)
                            <tr>
                                <td class="has-text-weight-bold">${s.getName()}</td>
                                <td>${s.getBranchName() != null ? s.getBranchName() : "-"}</td>
                                <td>
                                    <span class="tag is-info is-light is-rounded">${s.getStoreType() != null ? s.getStoreType().toString() : "GENERAL"}</span>
                                </td>
                                <td class="is-size-7 has-text-grey">${s.getAddress() != null ? s.getAddress() : "-"}</td>
                            </tr>
                        @endfor
                    @endif
                </tbody>
            </table>
        </div>

        <!-- MODAL: Crear Comercio -->
        <nano-modal id="modal-add-store" title="Registrar Comercio Habitual">
            <form id="form-add-store" action="/purchases/stores" method="post">
                <div class="field">
                    <label class="label">Nombre del Comercio <span class="has-text-danger">*</span></label>
                    <div class="control">
                        <input class="input" type="text" name="name" placeholder="Ej. Lider, Jumbo, Feria El Belloto" required>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Sucursal</label>
                    <div class="control">
                        <input class="input" type="text" name="branchName" placeholder="Ej. Centro, Sucursal Poniente">
                    </div>
                </div>

                <div class="field">
                    <label class="label">Tipo de Comercio</label>
                    <div class="control">
                        <div class="select is-fullwidth">
                            <select name="storeType">
                                <option value="SUPERMARKET">Supermercado</option>
                                <option value="LOCAL_STORE">Almacén de Barrio</option>
                                <option value="FARMERS_MARKET">Feria / Vega</option>
                                <option value="WHOLESALE">Mayorista</option>
                                <option value="PHARMACY">Farmacia</option>
                                <option value="OTHER">Otro</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label class="label">Dirección</label>
                    <div class="control">
                        <input class="input" type="text" name="address" placeholder="Av. Principal #123">
                    </div>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-add-store" class="button is-primary">Registrar Comercio</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>
    `
)
