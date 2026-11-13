@import java.util.List
@import cl.diego.NanoStock.catalog.dto.CategoryResponse
@import cl.diego.NanoStock.catalog.dto.BrandResponse
@import cl.diego.NanoStock.catalog.model.UnitOfMeasure
@import cl.diego.NanoStock.catalog.dto.CreateProductRequest

@param List<CategoryResponse> categories
@param List<BrandResponse> brands
@param UnitOfMeasure[] unitsOfMeasure
@param CreateProductRequest form
@param Long productId = null
@param String errorMessage = null
@param String flashMessage = null
@param String flashType = "is-success"

!{String currentPath = productId == null ? "/catalog/new" : "/catalog/" + productId + "/edit";}

@template.layouts.app(
    title = productId == null ? "Nuevo Producto" : "Editar Producto",
    activeSection = "catalog",
    content = @`
        <nav class="breadcrumb has-succeeds-separator is-small mb-4" aria-label="breadcrumbs">
            <ul>
                <li><a href="/catalog">Catálogo</a></li>
                <li class="is-active"><a href="#" aria-current="page">${productId == null ? "Nuevo Producto" : "Editar Producto"}</a></li>
            </ul>
        </nav>

        <div class="level mb-4">
            <div class="level-left">
                <div>
                    <h1 class="title is-4 mb-1">${productId == null ? "Registrar Nuevo Producto" : "Editar Producto Maestro"}</h1>
                    <p class="subtitle is-6 has-text-grey">Completa los atributos maestros y umbrales para inventario.</p>
                </div>
            </div>
            <div class="level-right">
                <a href="/catalog" class="button is-light">← Volver al Catálogo</a>
            </div>
        </div>

        @if(errorMessage != null)
            @template.components.alert(message = errorMessage, type = "is-danger", icon = "⚠️")
        @endif

        @if(flashMessage != null)
            @template.components.alert(message = flashMessage, type = flashType, icon = flashType.equals("is-danger") ? "⚠️" : "✅")
        @endif

        <div class="box">
            <form action="${productId == null ? "/catalog" : "/catalog/" + productId}" method="post">
                <div class="columns is-multiline">
                    <!-- Nombre -->
                    <div class="column is-8">
                        <div class="field">
                            <label class="label">Nombre del Producto <span class="has-text-danger">*</span></label>
                            <div class="control">
                                <input class="input" type="text" name="name" value="${form.getName() != null ? form.getName() : ""}" placeholder="Ej. Leche Entera 1L" required autofocus>
                            </div>
                        </div>
                    </div>

                    <!-- Código de Barras con Scanner -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Código de Barras <span class="has-text-danger">*</span></label>
                            <div class="field has-addons">
                                <div class="control is-expanded">
                                    <input id="barCodeInput" class="input" type="text" name="barCode" value="${form.getBarCode() != null ? form.getBarCode() : ""}" placeholder="780123456789" required>
                                </div>
                                <div class="control">
                                    <nano-barcode-scanner target-input="#barCodeInput" button-text=""></nano-barcode-scanner>
                                </div>
                            </div>
                            <p class="help">EAN, UPC o código generado por el hogar.</p>
                        </div>
                    </div>

                    <!-- Descripción -->
                    <div class="column is-12">
                        <div class="field">
                            <label class="label">Descripción / Notas Adicionales</label>
                            <div class="control">
                                <textarea class="textarea has-fixed-size" rows="2" name="description" placeholder="Detalles de empaque, especificaciones...">${form.getDescription() != null ? form.getDescription() : ""}</textarea>
                            </div>
                        </div>
                    </div>

                    <!-- SKU -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">SKU (Opcional)</label>
                            <div class="control">
                                <input class="input" type="text" name="sku" value="${form.getSku() != null ? form.getSku() : ""}" placeholder="Generado automático si vacío">
                            </div>
                        </div>
                    </div>

                    <!-- Categoría -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Categoría</label>
                            <div class="field has-addons">
                                <div class="control is-expanded">
                                    <div class="select is-fullwidth">
                                        <select name="categoryId">
                                            <option value="">-- Sin Categoría --</option>
                                            @for(CategoryResponse cat : categories)
                                                <option value="${cat.getId()}" selected="${form.getCategoryId() != null && form.getCategoryId().equals(cat.getId())}">
                                                    ${cat.getName()}
                                                </option>
                                            @endfor
                                        </select>
                                    </div>
                                </div>
                                <div class="control">
                                    <button type="button" class="button is-light" title="Crear nueva categoría" onclick="document.getElementById('modal-quick-category').open()">+</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Marca -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Marca</label>
                            <div class="field has-addons">
                                <div class="control is-expanded">
                                    <div class="select is-fullwidth">
                                        <select name="brandId">
                                            <option value="">-- Genérica / Sin Marca --</option>
                                            @for(BrandResponse brand : brands)
                                                <option value="${brand.getId()}" selected="${form.getBrandId() != null && form.getBrandId().equals(brand.getId())}">
                                                    ${brand.getName()}
                                                </option>
                                            @endfor
                                        </select>
                                    </div>
                                </div>
                                <div class="control">
                                    <button type="button" class="button is-light" title="Crear nueva marca" onclick="document.getElementById('modal-quick-brand').open()">+</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Unidad de Medida -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Unidad de Medida</label>
                            <div class="control">
                                <div class="select is-fullwidth">
                                    <select name="unitOfMeasure">
                                        @for(UnitOfMeasure uom : unitsOfMeasure)
                                            <option value="${uom.name()}" selected="${form.getUnitOfMeasure() != null && form.getUnitOfMeasure().equals(uom)}">
                                                ${uom.getDisplayName()} (${uom.getSymbol()})
                                            </option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Umbral Stock Mínimo -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Stock Mínimo de Seguridad</label>
                            <div class="control">
                                <input class="input" type="number" step="0.1" min="0" name="minStockThreshold" value="${form.getMinStockThreshold() != null ? form.getMinStockThreshold().toString() : "1.0"}" required>
                            </div>
                            <p class="help">Dispara alerta de reposición si las existencias caen a este nivel.</p>
                        </div>
                    </div>

                    <!-- Stock Meta -->
                    <div class="column is-4">
                        <div class="field">
                            <label class="label">Stock Deseado / Meta</label>
                            <div class="control">
                                <input class="input" type="number" step="0.1" min="0" name="targetStockQuantity" value="${form.getTargetStockQuantity() != null ? form.getTargetStockQuantity().toString() : "3.0"}" required>
                            </div>
                            <p class="help">Cantidad sugerida al autocompletar la lista de compras.</p>
                        </div>
                    </div>

                    <!-- Es Perecible -->
                    <div class="column is-12">
                        <div class="field">
                            <div class="control">
                                <label class="checkbox">
                                    <input type="checkbox" name="isPerishable" value="true" checked="${form.getIsPerishable() == null || form.getIsPerishable()}">
                                    <span class="has-text-weight-semibold ml-2">¿Es producto perecible?</span>
                                    <span class="has-text-grey is-size-7 is-block ml-5">
                                        Habilita trazabilidad FEFO por lote y control riguroso de fechas de vencimiento.
                                    </span>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="field is-grouped mt-5">
                    <div class="control">
                        <button type="submit" class="button is-primary">
                            <span>💾</span>
                            <span class="ml-1">${productId == null ? "Guardar Producto" : "Guardar Cambios"}</span>
                        </button>
                    </div>
                    <div class="control">
                        <a href="/catalog" class="button is-light">Cancelar</a>
                    </div>
                </div>
            </form>
        </div>

        <!-- MODAL: Nueva Categoría Rápida -->
        <nano-modal id="modal-quick-category" title="Nueva Categoría">
            <form id="form-quick-category" action="/catalog/categories" method="post">
                <input type="hidden" name="returnTo" value="${currentPath}">
                <div class="field">
                    <label class="label">Nombre <span class="has-text-danger">*</span></label>
                    <input class="input" type="text" name="name" placeholder="Ej. Lácteos" required>
                </div>
                <div class="field">
                    <label class="label">Descripción</label>
                    <input class="input" type="text" name="description" placeholder="Opcional">
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-quick-category" class="button is-primary">Crear Categoría</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>

        <!-- MODAL: Nueva Marca Rápida -->
        <nano-modal id="modal-quick-brand" title="Nueva Marca">
            <form id="form-quick-brand" action="/catalog/brands" method="post">
                <input type="hidden" name="returnTo" value="${currentPath}">
                <div class="field">
                    <label class="label">Nombre <span class="has-text-danger">*</span></label>
                    <input class="input" type="text" name="name" placeholder="Ej. Soprole" required>
                </div>
            </form>
            <div slot="footer">
                <button type="submit" form="form-quick-brand" class="button is-primary">Crear Marca</button>
                <button type="button" class="button" data-close>Cancelar</button>
            </div>
        </nano-modal>
    `,
    scripts = @`
        <script type="module" src="/js/components/nano-barcode-scanner.js"></script>
    `
)
