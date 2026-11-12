package cl.diego.NanoStock.catalog.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.dto.BrandRequest;
import cl.diego.NanoStock.catalog.dto.BrandResponse;
import cl.diego.NanoStock.catalog.dto.CategoryRequest;
import cl.diego.NanoStock.catalog.dto.CategoryResponse;
import cl.diego.NanoStock.catalog.dto.CreateProductRequest;
import cl.diego.NanoStock.catalog.dto.ProductResponse;
import cl.diego.NanoStock.catalog.dto.UpdateProductRequest;
import cl.diego.NanoStock.catalog.event.ProductCreatedEvent;
import cl.diego.NanoStock.catalog.event.ProductUpdatedEvent;
import cl.diego.NanoStock.catalog.model.Brand;
import cl.diego.NanoStock.catalog.model.Category;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.BrandRepository;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductCatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ApplicationEventPublisher eventPublisher;

    // --- Métodos de Categorías ---

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getName());
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría padre no encontrada: " + request.getParentId()));
        }

        Category category = Category.builder()
            .name(request.getName().trim())
            .description(request.getDescription())
            .parent(parent)
            .build();

        return toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::toCategoryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(this::toCategoryResponse);
    }

    // --- Métodos de Marcas ---

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (brandRepository.existsByName(request.getName().trim())) {
            throw new IllegalArgumentException("Ya existe la marca: " + request.getName());
        }

        Brand brand = Brand.builder()
            .name(request.getName().trim())
            .build();

        return toBrandResponse(brandRepository.save(brand));
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
            .map(this::toBrandResponse)
            .toList();
    }

    @Transactional
    public Brand getOrCreateBrand(String name) {
        String cleanName = name.trim();
        return brandRepository.findByName(cleanName)
            .orElseGet(() -> brandRepository.save(new Brand(cleanName)));
    }

    // --- Métodos de Productos ---

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByBarCode(request.getBarCode().trim())) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + request.getBarCode());
        }

        String sku = request.getSku() != null && !request.getSku().isBlank()
            ? request.getSku().trim().toUpperCase(Locale.ROOT)
            : generateSku(request.getName());

        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("Ya existe un producto con el SKU: " + sku);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + request.getCategoryId()));
        }

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + request.getBrandId()));
        }

        Product product = Product.builder()
            .name(request.getName().trim())
            .description(request.getDescription())
            .sku(sku)
            .barCode(request.getBarCode().trim())
            .category(category)
            .brand(brand)
            .unitOfMeasure(request.getUnitOfMeasure() != null ? request.getUnitOfMeasure() : UnitOfMeasure.UNIT)
            .minStockThreshold(request.getMinStockThreshold() != null ? request.getMinStockThreshold() : 1.0)
            .targetStockQuantity(request.getTargetStockQuantity() != null ? request.getTargetStockQuantity() : 3.0)
            .isPerishable(request.getIsPerishable() != null ? request.getIsPerishable() : true)
            .build();

        Product saved = productRepository.save(product);

        // Publicar evento de dominio
        eventPublisher.publishEvent(new ProductCreatedEvent(
            saved.getId(),
            saved.getName(),
            saved.getBarCode(),
            saved.getSku(),
            saved.getMinStockThreshold(),
            saved.getTargetStockQuantity()
        ));

        return toProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        String newBarCode = request.getBarCode().trim();
        if (!product.getBarCode().equalsIgnoreCase(newBarCode) && productRepository.existsByBarCode(newBarCode)) {
            throw new IllegalArgumentException("El código de barras ya pertenece a otro producto: " + newBarCode);
        }

        String newSku = request.getSku() != null && !request.getSku().isBlank()
            ? request.getSku().trim().toUpperCase(Locale.ROOT)
            : product.getSku();

        if (!product.getSku().equalsIgnoreCase(newSku) && productRepository.existsBySku(newSku)) {
            throw new IllegalArgumentException("El SKU ya pertenece a otro producto: " + newSku);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada: " + request.getBrandId()));
            product.setBrand(brand);
        }

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setSku(newSku);
        product.setBarCode(newBarCode);

        if (request.getUnitOfMeasure() != null) {
            product.setUnitOfMeasure(request.getUnitOfMeasure());
        }
        if (request.getMinStockThreshold() != null) {
            product.setMinStockThreshold(request.getMinStockThreshold());
        }
        if (request.getTargetStockQuantity() != null) {
            product.setTargetStockQuantity(request.getTargetStockQuantity());
        }
        if (request.getIsPerishable() != null) {
            product.setPerishable(request.getIsPerishable());
        }

        Product updated = productRepository.save(product);

        eventPublisher.publishEvent(new ProductUpdatedEvent(
            updated.getId(),
            updated.getName(),
            updated.getBarCode(),
            updated.getSku()
        ));

        return toProductResponse(updated);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id).map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductByBarCode(String barCode) {
        return productRepository.findByBarCode(barCode.trim()).map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductBySku(String sku) {
        return productRepository.findBySku(sku.trim().toUpperCase(Locale.ROOT)).map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepository.findByNameContainingIgnoreCase(query.trim()).stream()
            .map(this::toProductResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(this::toProductResponse)
            .toList();
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // --- Mappings y Helpers ---

    private String generateSku(String name) {
        String clean = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
        String prefix = clean.length() >= 3 ? clean.substring(0, 3) : "PRD";
        String suffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase(Locale.ROOT);
        return prefix + "-" + suffix;
    }

    public ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .barCode(product.getBarCode())
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
            .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
            .unitOfMeasure(product.getUnitOfMeasure())
            .minStockThreshold(product.getMinStockThreshold())
            .targetStockQuantity(product.getTargetStockQuantity())
            .isPerishable(product.isPerishable())
            .createdAt(product.getCreatedAt())
            .build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .description(category.getDescription())
            .parentId(category.getParent() != null ? category.getParent().getId() : null)
            .parentName(category.getParent() != null ? category.getParent().getName() : null)
            .createdAt(category.getCreatedAt())
            .build();
    }

    private BrandResponse toBrandResponse(Brand brand) {
        return BrandResponse.builder()
            .id(brand.getId())
            .name(brand.getName())
            .createdAt(brand.getCreatedAt())
            .build();
    }
}
