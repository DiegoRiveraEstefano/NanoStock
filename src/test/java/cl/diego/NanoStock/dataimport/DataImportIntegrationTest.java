package cl.diego.NanoStock.dataimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.diego.NanoStock.catalog.event.ProductCreatedEvent;
import cl.diego.NanoStock.catalog.event.ProductUpdatedEvent;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.BrandRepository;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.dto.ImportConflictStrategy;
import cl.diego.NanoStock.dataimport.dto.ImportResultDto;
import cl.diego.NanoStock.dataimport.service.InventoryImportService;
import cl.diego.NanoStock.dataimport.service.ProductImportService;
import cl.diego.NanoStock.inventory.event.StockLevelChangedEvent;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.Location;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
@RecordApplicationEvents
class DataImportIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DatabaseCleanupHelper cleanupHelper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private ProductImportService productImportService;

    @Autowired
    private InventoryImportService inventoryImportService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ApplicationEvents applicationEvents;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cleanupHelper.cleanAll();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("Debe importar productos desde archivo Excel (.xlsx) correctamente")
    void shouldImportProductsFromExcel() throws Exception {
        byte[] excelBytes = createSampleProductExcel();

        MockMultipartFile file = new MockMultipartFile(
            "file", "catalogo.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelBytes
        );

        ImportResultDto result = productImportService.importProducts(
            file.getInputStream(), DataFormat.EXCEL, false, ImportConflictStrategy.UPDATE, true
        );

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getErrorCount()).isZero();

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(2);

        Product leche = productRepository.findByBarCode("7801111111111").orElseThrow();
        assertThat(leche.getName()).isEqualTo("Leche Descremada 1L");
        assertThat(leche.getCategory()).isNotNull();
        assertThat(leche.getCategory().getName()).isEqualTo("Lácteos");
        assertThat(leche.getBrand()).isNotNull();
        assertThat(leche.getBrand().getName()).isEqualTo("Colun");
        assertThat(leche.getUnitOfMeasure()).isEqualTo(UnitOfMeasure.LITER);
        assertThat(leche.getMinStockThreshold()).isEqualTo(2.0);

        long eventCount = applicationEvents.stream(ProductCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe validar en modo Dry-Run sin persistir cambios en la base de datos")
    void shouldPerformDryRunWithoutSavingToDatabase() throws Exception {
        String csvContent = """
            Nombre,Descripcion,CodigoBarras,SKU,Categoria,Marca,UnidadMedida,StockMinimo,StockOptimo,EsPerecible
            Fideos Espagueti,Bolsa 400g,7802222222222,FID-ESP-01,Pastas,Lucchetti,PACKAGE,1.0,3.0,NO
            Atun en Lomitos,Lata 160g,7803333333333,ATU-LOM-01,Conservas,San Jose,CAN,2.0,5.0,NO
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "dryrun.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDto result = productImportService.importProducts(
            file.getInputStream(), DataFormat.CSV, true, ImportConflictStrategy.FAIL, true
        );

        assertThat(result.isDryRun()).isTrue();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getErrorCount()).isZero();

        // En Dry Run NO debe haberse persistido ningún registro en base de datos
        assertThat(productRepository.count()).isZero();
        assertThat(categoryRepository.count()).isZero();
        assertThat(brandRepository.count()).isZero();
    }

    @Test
    @DisplayName("Estrategia de conflicto UPDATE: Debe actualizar atributos de producto existente")
    void shouldUpdateExistingProductOnConflict() throws Exception {
        // Producto preexistente
        Product existing = Product.builder()
            .name("Aceite Maravilla 900ml")
            .barCode("7804444444444")
            .sku("ACE-MAR-01")
            .minStockThreshold(1.0)
            .targetStockQuantity(2.0)
            .build();
        productRepository.save(existing);

        String jsonContent = """
            [
              {
                "name": "Aceite Maravilla Premium 1L",
                "barCode": "7804444444444",
                "minStockThreshold": 3.0,
                "targetStockQuantity": 6.0,
                "categoryName": "Aceites",
                "brandName": "Chef"
              }
            ]
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "products.json", "application/json", jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDto result = productImportService.importProducts(
            file.getInputStream(), DataFormat.JSON, false, ImportConflictStrategy.UPDATE, true
        );

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getErrorCount()).isZero();

        Product updated = productRepository.findByBarCode("7804444444444").orElseThrow();
        assertThat(updated.getName()).isEqualTo("Aceite Maravilla Premium 1L");
        assertThat(updated.getMinStockThreshold()).isEqualTo(3.0);
        assertThat(updated.getTargetStockQuantity()).isEqualTo(6.0);
        assertThat(updated.getCategory().getName()).isEqualTo("Aceites");

        long updatedEvents = applicationEvents.stream(ProductUpdatedEvent.class).count();
        assertThat(updatedEvents).isEqualTo(1);
    }

    @Test
    @DisplayName("Estrategia de conflicto SKIP: Debe omitir productos existentes sin error")
    void shouldSkipExistingProductOnConflict() throws Exception {
        Product existing = Product.builder()
            .name("Sal Fina")
            .barCode("7805555555555")
            .sku("SAL-FIN-01")
            .build();
        productRepository.save(existing);

        String jsonContent = """
            [
              { "name": "Sal Fina Modificada", "barCode": "7805555555555" },
              { "name": "Pimienta Negra", "barCode": "7806666666666" }
            ]
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "products.json", "application/json", jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDto result = productImportService.importProducts(
            file.getInputStream(), DataFormat.JSON, false, ImportConflictStrategy.SKIP, true
        );

        assertThat(result.getSuccessCount()).isEqualTo(1); // Pimienta
        assertThat(result.getSkippedCount()).isEqualTo(1); // Sal
        assertThat(result.getErrorCount()).isZero();

        Product sal = productRepository.findByBarCode("7805555555555").orElseThrow();
        assertThat(sal.getName()).isEqualTo("Sal Fina"); // No modificada
    }

    @Test
    @DisplayName("Debe reportar incidencias detalladas por fila (errores de validación)")
    void shouldReportRowLevelValidationErrors() throws Exception {
        String csvContent = """
            Nombre,Descripcion,CodigoBarras,SKU,Categoria,Marca,UnidadMedida,StockMinimo,StockOptimo,EsPerecible
            ,Fila sin nombre,7807777777777,ERR-01,Test,Test,UNIT,1.0,3.0,NO
            Producto Sin Barcode,Descripcion,,ERR-02,Test,Test,UNIT,1.0,3.0,NO
            Producto Stock Negativo,Descripcion,7808888888888,ERR-03,Test,Test,UNIT,-5.0,3.0,NO
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "invalid.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDto result = productImportService.importProducts(
            file.getInputStream(), DataFormat.CSV, false, ImportConflictStrategy.FAIL, true
        );

        assertThat(result.getErrorCount()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).hasSize(3);
        assertThat(result.getErrors().get(0).getField()).isEqualTo("name");
        assertThat(result.getErrors().get(1).getField()).isEqualTo("barCode");
        assertThat(result.getErrors().get(2).getField()).isEqualTo("minStockThreshold");
    }

    @Test
    @DisplayName("Debe importar inventario y generar lotes, bitácora de movimiento y eventos")
    void shouldImportInventoryAndCreateBatchesAndMovements() throws Exception {
        Product product = Product.builder()
            .name("Café Grano 500g")
            .barCode("7809999999999")
            .sku("CAF-GRA-01")
            .unitOfMeasure(UnitOfMeasure.GRAM)
            .build();
        productRepository.save(product);

        String jsonContent = """
            [
              {
                "productIdentifier": "7809999999999",
                "locationName": "Despensa Alta",
                "storageType": "AMBIENT",
                "quantity": 3.0,
                "expirationDate": "2027-12-31",
                "sealedStatus": "SEALED",
                "batchCode": "LOTE-CAF-001"
              }
            ]
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "inventory.json", "application/json", jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportResultDto result = inventoryImportService.importInventory(
            file.getInputStream(), DataFormat.JSON, false, true
        );

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        // Verificar lote creado
        List<StockBatch> batches = stockBatchRepository.findByProductId(product.getId());
        assertThat(batches).hasSize(1);
        StockBatch batch = batches.get(0);
        assertThat(batch.getQuantity()).isEqualTo(3.0);
        assertThat(batch.getLocation().getName()).isEqualTo("Despensa Alta");
        assertThat(batch.getBatchCode()).isEqualTo("LOTE-CAF-001");
        assertThat(batch.getExpirationDate()).isEqualTo(LocalDate.of(2027, 12, 31));

        // Verificar movimiento ENTRY de auditoría
        List<InventoryMovement> movements = movementRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).getMovementType()).isEqualTo(MovementType.ENTRY);
        assertThat(movements.get(0).getQuantity()).isEqualTo(3.0);

        // Verificar evento emitido
        long stockEvents = applicationEvents.stream(StockLevelChangedEvent.class).count();
        assertThat(stockEvents).isGreaterThanOrEqualTo(1);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("REST API: Debe procesar importación vía endpoint HTTP multipart")
    void shouldImportViaRestMultipartEndpoint() throws Exception {
        String csvContent = """
            Nombre,Descripcion,CodigoBarras,SKU,Categoria,Marca,UnidadMedida,StockMinimo,StockOptimo,EsPerecible
            Yogurt Griego,Natural 150g,7801231231234,YOG-GRI-01,Lácteos,Chobani,UNIT,2.0,6.0,SI
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "yogurt.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/import/products")
                .file(file)
                .param("dryRun", "false")
                .param("conflictStrategy", "UPDATE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.successCount").value(1))
            .andExpect(jsonPath("$.errorCount").value(0))
            .andExpect(jsonPath("$.entityType").value("PRODUCT"));

        assertThat(productRepository.existsByBarCode("7801231231234")).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("REST API: Debe descargar plantilla de importación en Excel y CSV")
    void shouldDownloadImportTemplates() throws Exception {
        mockMvc.perform(get("/api/import/templates/products").param("format", "EXCEL"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"plantilla_products.xlsx\""));

        mockMvc.perform(get("/api/import/templates/inventory").param("format", "CSV"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"plantilla_inventory.csv\""));
    }

    private byte[] createSampleProductExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nombre");
            header.createCell(1).setCellValue("Descripcion");
            header.createCell(2).setCellValue("CodigoBarras");
            header.createCell(3).setCellValue("SKU");
            header.createCell(4).setCellValue("Categoria");
            header.createCell(5).setCellValue("Marca");
            header.createCell(6).setCellValue("UnidadMedida");
            header.createCell(7).setCellValue("StockMinimo");
            header.createCell(8).setCellValue("StockOptimo");
            header.createCell(9).setCellValue("EsPerecible");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Leche Descremada 1L");
            r1.createCell(1).setCellValue("Caja larga vida");
            r1.createCell(2).setCellValue("7801111111111");
            r1.createCell(3).setCellValue("LEC-DES-01");
            r1.createCell(4).setCellValue("Lácteos");
            r1.createCell(5).setCellValue("Colun");
            r1.createCell(6).setCellValue("LITER");
            r1.createCell(7).setCellValue(2.0);
            r1.createCell(8).setCellValue(6.0);
            r1.createCell(9).setCellValue("SI");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Queso Gouda 250g");
            r2.createCell(1).setCellValue("Laminado");
            r2.createCell(2).setCellValue("7801111111112");
            r2.createCell(3).setCellValue("QUE-GOU-01");
            r2.createCell(4).setCellValue("Lácteos");
            r2.createCell(5).setCellValue("Soprole");
            r2.createCell(6).setCellValue("GRAM");
            r2.createCell(7).setCellValue(1.0);
            r2.createCell(8).setCellValue(3.0);
            r2.createCell(9).setCellValue("SI");

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
