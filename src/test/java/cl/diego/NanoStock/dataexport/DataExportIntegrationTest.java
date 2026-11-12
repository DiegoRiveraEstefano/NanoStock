package cl.diego.NanoStock.dataexport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.catalog.model.Brand;
import cl.diego.NanoStock.catalog.model.Category;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.BrandRepository;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataexport.dto.ExportDataResult;
import cl.diego.NanoStock.dataexport.dto.InventoryExportFilter;
import cl.diego.NanoStock.dataexport.dto.MovementExportFilter;
import cl.diego.NanoStock.dataexport.dto.ProductExportDto;
import cl.diego.NanoStock.dataexport.dto.ProductExportFilter;
import cl.diego.NanoStock.dataexport.service.InventoryExportService;
import cl.diego.NanoStock.dataexport.service.MovementExportService;
import cl.diego.NanoStock.dataexport.service.ProductExportService;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.Location;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.testutil.DatabaseCleanupHelper;

@SpringBootTest
class DataExportIntegrationTest {

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
    private ProductExportService productExportService;

    @Autowired
    private InventoryExportService inventoryExportService;

    @Autowired
    private MovementExportService movementExportService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cleanupHelper.cleanAll();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("Debe exportar catálogo completo a Excel (.xlsx) con metadatos y encabezados estilizados")
    void shouldExportAllProductsToExcel() throws Exception {
        seedProducts();

        ExportDataResult result = productExportService.exportProducts(null, DataFormat.EXCEL);

        assertThat(result.getRecordCount()).isEqualTo(3);
        assertThat(result.getFilename()).endsWith(".xlsx");
        assertThat(result.getContentType()).isEqualTo(DataFormat.EXCEL.getMediaType());
        assertThat(result.getData()).isNotEmpty();

        // Validar formato POI XSSFWorkbook
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(result.getData()))) {
            Sheet sheet = wb.getSheet("Productos");
            assertThat(sheet).isNotNull();
            Row header = sheet.getRow(0);
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Nombre");
            assertThat(header.getCell(4).getStringCellValue()).isEqualTo("CodigoBarras");

            // Fila de datos 1
            Row r1 = sheet.getRow(1);
            assertThat(r1).isNotNull();
            assertThat(r1.getCell(1).getStringCellValue()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Debe exportar a CSV y JSON respetando filtros de búsqueda y categoría")
    void shouldExportProductsWithFiltersToCsvAndJson() throws Exception {
        Category lacteos = categoryRepository.save(Category.builder().name("Lácteos").description("Productos lácteos").build());
        Category despensa = categoryRepository.save(Category.builder().name("Despensa").description("Abarrotes").build());

        Product p1 = Product.builder().name("Leche Entera").barCode("111").sku("LEC-01").category(lacteos).build();
        Product p2 = Product.builder().name("Yogurt Natural").barCode("222").sku("YOG-01").category(lacteos).build();
        Product p3 = Product.builder().name("Arroz Grado 1").barCode("333").sku("ARR-01").category(despensa).build();
        productRepository.saveAll(List.of(p1, p2, p3));

        // Filtro por categoría Lácteos
        ProductExportFilter filter = ProductExportFilter.builder()
            .categoryId(lacteos.getId())
            .build();

        // Exportar JSON
        ExportDataResult jsonResult = productExportService.exportProducts(filter, DataFormat.JSON);
        assertThat(jsonResult.getRecordCount()).isEqualTo(2);

        List<ProductExportDto> dtoList = objectMapper.readValue(
            jsonResult.getData(), new TypeReference<List<ProductExportDto>>() {}
        );
        assertThat(dtoList).hasSize(2);
        assertThat(dtoList).extracting(ProductExportDto::getCategoryName).containsOnly("Lácteos");

        // Exportar CSV
        ExportDataResult csvResult = productExportService.exportProducts(filter, DataFormat.CSV);
        String csvText = new String(csvResult.getData(), StandardCharsets.UTF_8);
        assertThat(csvText).contains("Leche Entera");
        assertThat(csvText).contains("Yogurt Natural");
        assertThat(csvText).doesNotContain("Arroz Grado 1");
    }

    @Test
    @DisplayName("Debe filtrar productos en alerta de stock crítico/bajo stock")
    void shouldFilterProductsByLowStock() throws Exception {
        Product p1 = Product.builder().name("Harina").barCode("444").sku("HAR-01").minStockThreshold(5.0).build();
        Product p2 = Product.builder().name("Azúcar").barCode("555").sku("AZU-01").minStockThreshold(2.0).build();
        productRepository.saveAll(List.of(p1, p2));

        Location loc = locationRepository.save(new Location("Despensa", StorageType.AMBIENT, ""));
        // Harina tiene 1.0 (menor a 5.0 -> alerta de bajo stock)
        stockBatchRepository.save(StockBatch.builder().product(p1).location(loc).quantity(1.0).build());
        // Azúcar tiene 10.0 (mayor a 2.0 -> stock saludable)
        stockBatchRepository.save(StockBatch.builder().product(p2).location(loc).quantity(10.0).build());

        ProductExportFilter filter = ProductExportFilter.builder()
            .onlyLowStock(true)
            .build();

        ExportDataResult result = productExportService.exportProducts(filter, DataFormat.JSON);
        assertThat(result.getRecordCount()).isEqualTo(1);

        List<ProductExportDto> dtoList = objectMapper.readValue(
            result.getData(), new TypeReference<List<ProductExportDto>>() {}
        );
        assertThat(dtoList.get(0).getName()).isEqualTo("Harina");
        assertThat(dtoList.get(0).getIsLowStock()).isTrue();
    }

    @Test
    @DisplayName("Seguridad: Mitigación de Fórmula Injection (CSV / Excel Injection)")
    void shouldSanitizeDangerousPrefixesToPreventFormulaInjection() throws Exception {
        // Un nombre malicioso que comienza con fórmula de Excel: =cmd|' /C calc'!A0
        Product dangerous = Product.builder()
            .name("=cmd|'/C calc'!A0")
            .description("+123456789")
            .barCode("777")
            .sku("DNG-01")
            .build();
        productRepository.save(dangerous);

        // Exportar CSV
        ExportDataResult csvResult = productExportService.exportProducts(null, DataFormat.CSV);
        String csvText = new String(csvResult.getData(), StandardCharsets.UTF_8);

        // En CSV el prefijo peligroso debe quedar neutralizado con apóstrofe inicial
        assertThat(csvText).contains("'=cmd|'/C calc'!A0");
        assertThat(csvText).contains("'+123456789");

        // Exportar Excel
        ExportDataResult excelResult = productExportService.exportProducts(null, DataFormat.EXCEL);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(excelResult.getData()))) {
            Sheet sheet = wb.getSheet("Productos");
            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(1).getStringCellValue()).startsWith("'=");
        }
    }

    @Test
    @DisplayName("Debe exportar inventario filtrando por ubicación y lotes próximos a vencer")
    void shouldExportInventoryWithFilters() throws Exception {
        Product p = productRepository.save(Product.builder().name("Yogurt").barCode("888").sku("YOG-888").build());
        Location loc1 = locationRepository.save(new Location("Refrigerador", StorageType.REFRIGERATED, ""));
        Location loc2 = locationRepository.save(new Location("Freezer", StorageType.FROZEN, ""));

        // Lote 1: vence en 3 días en Refrigerador
        stockBatchRepository.save(StockBatch.builder()
            .product(p).location(loc1).quantity(2.0)
            .expirationDate(LocalDate.now().plusDays(3))
            .batchCode("LOTE-REF")
            .build());

        // Lote 2: vence en 60 días en Freezer
        stockBatchRepository.save(StockBatch.builder()
            .product(p).location(loc2).quantity(4.0)
            .expirationDate(LocalDate.now().plusDays(60))
            .batchCode("LOTE-FRZ")
            .build());

        // Filtro: vencer antes de 7 días
        InventoryExportFilter filter = InventoryExportFilter.builder()
            .expiringBefore(LocalDate.now().plusDays(7))
            .build();

        ExportDataResult result = inventoryExportService.exportInventory(filter, DataFormat.JSON);
        assertThat(result.getRecordCount()).isEqualTo(1);
        String json = new String(result.getData(), StandardCharsets.UTF_8);
        assertThat(json).contains("LOTE-REF");
        assertThat(json).doesNotContain("LOTE-FRZ");
    }

    @Test
    @DisplayName("Debe exportar bitácora de movimientos filtrada por tipo de movimiento")
    void shouldExportMovementsFilteredByType() throws Exception {
        Product p = productRepository.save(Product.builder().name("Atún").barCode("999").sku("ATU-999").build());

        movementRepository.save(InventoryMovement.builder()
            .product(p).movementType(MovementType.ENTRY).quantity(5.0).notes("Entrada por compra").build());
        movementRepository.save(InventoryMovement.builder()
            .product(p).movementType(MovementType.CONSUMPTION).quantity(1.0).notes("Consumo cena").build());
        movementRepository.save(InventoryMovement.builder()
            .product(p).movementType(MovementType.WASTE).quantity(1.0).wasteReason("Vencido").build());

        MovementExportFilter filter = MovementExportFilter.builder()
            .movementType(MovementType.WASTE)
            .build();

        ExportDataResult result = movementExportService.exportMovements(filter, DataFormat.CSV);
        assertThat(result.getRecordCount()).isEqualTo(1);
        String csv = new String(result.getData(), StandardCharsets.UTF_8);
        assertThat(csv).contains("WASTE");
        assertThat(csv).contains("Vencido");
        assertThat(csv).doesNotContain("CONSUMPTION");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("REST API: Endpoints GET y POST de exportación con cabeceras correctas")
    void shouldExportViaRestEndpoints() throws Exception {
        seedProducts();

        // GET /api/export/products
        mockMvc.perform(get("/api/export/products")
                .param("format", "EXCEL")
                .param("search", "Leche"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("productos_export_")))
            .andExpect(header().string("X-Total-Count", "1"));

        // POST /api/export/products con JSON body
        String filterJson = """
            {
              "search": "Café"
            }
            """;
        mockMvc.perform(post("/api/export/products")
                .param("format", "CSV")
                .contentType(MediaType.APPLICATION_JSON)
                .content(filterJson))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")))
            .andExpect(header().string("X-Total-Count", "1"));

        // GET /api/export/inventory
        mockMvc.perform(get("/api/export/inventory").param("format", "JSON"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".json")));

        // GET /api/export/movements
        mockMvc.perform(get("/api/export/movements").param("format", "CSV"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".csv")));
    }

    private void seedProducts() {
        Category cat = categoryRepository.save(Category.builder().name("General").description("").build());
        Brand brand = brandRepository.save(new Brand("Marca X"));

        Product p1 = Product.builder().name("Leche Entera").barCode("1001").sku("SKU-1001").category(cat).brand(brand).build();
        Product p2 = Product.builder().name("Café Tostado").barCode("1002").sku("SKU-1002").category(cat).brand(brand).build();
        Product p3 = Product.builder().name("Té Negro").barCode("1003").sku("SKU-1003").category(cat).brand(brand).build();

        productRepository.saveAll(List.of(p1, p2, p3));
    }
}
