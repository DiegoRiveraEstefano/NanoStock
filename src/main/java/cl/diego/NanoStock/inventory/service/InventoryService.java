package cl.diego.NanoStock.inventory.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.iam.model.User;
import cl.diego.NanoStock.iam.repository.UserRepository;
import cl.diego.NanoStock.inventory.dto.AddStockBatchRequest;
import cl.diego.NanoStock.inventory.dto.ConsumeStockRequest;
import cl.diego.NanoStock.inventory.dto.InventoryMovementResponse;
import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.dto.ProductStockOverview;
import cl.diego.NanoStock.inventory.dto.RegisterWasteRequest;
import cl.diego.NanoStock.inventory.dto.StockAdjustmentRequest;
import cl.diego.NanoStock.inventory.dto.StockBatchResponse;
import cl.diego.NanoStock.inventory.event.StockConsumedEvent;
import cl.diego.NanoStock.inventory.event.StockLevelChangedEvent;
import cl.diego.NanoStock.inventory.event.StockWastedEvent;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.Location;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final LocationRepository locationRepository;
    private final StockBatchRepository stockBatchRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    // --- Gestión de Ubicaciones ---

    @Transactional
    public LocationResponse createLocation(LocationRequest request) {
        String cleanName = request.getName().trim();
        if (locationRepository.existsByNameIgnoreCase(cleanName)) {
            throw new IllegalArgumentException("Ya existe una ubicación con el nombre: " + cleanName);
        }

        Location location = Location.builder()
            .name(cleanName)
            .storageType(request.getStorageType() != null ? request.getStorageType() : StorageType.AMBIENT)
            .description(request.getDescription() != null ? request.getDescription().trim() : null)
            .build();

        return toLocationResponse(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        return locationRepository.findAll().stream()
            .map(this::toLocationResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LocationResponse> getLocationById(Long id) {
        return locationRepository.findById(id).map(this::toLocationResponse);
    }

    @Transactional
    public LocationResponse updateLocation(Long id, LocationRequest request) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada con ID: " + id));

        String cleanName = request.getName().trim();
        if (locationRepository.existsByNameIgnoreCaseAndIdNot(cleanName, id)) {
            throw new IllegalArgumentException("Ya existe otra ubicación con el nombre: " + cleanName);
        }

        location.setName(cleanName);
        if (request.getStorageType() != null) {
            location.setStorageType(request.getStorageType());
        }
        location.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        return toLocationResponse(locationRepository.save(location));
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada con ID: " + id));

        long activeBatches = stockBatchRepository.countByLocationIdAndQuantityGreaterThan(id, 0.0);
        if (activeBatches > 0) {
            throw new IllegalArgumentException(
                "No se puede eliminar la ubicación '" + location.getName()
                    + "' porque aún tiene " + activeBatches + " lote(s) con stock activo.");
        }

        // Desvincular movimientos históricos y limpiar lotes ya agotados (quantity == 0) para respetar la FK
        movementRepository.detachBatchesByLocationId(id);
        movementRepository.detachLocationById(id);
        if (stockBatchRepository.existsByLocationId(id)) {
            stockBatchRepository.deleteByLocationId(id);
        }

        locationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<StockBatchResponse> getActiveBatchesByLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new IllegalArgumentException("Ubicación no encontrada con ID: " + locationId);
        }

        return stockBatchRepository.findByLocationIdAndQuantityGreaterThan(locationId, 0.0).stream()
            .map(this::toStockBatchResponse)
            .toList();
    }

    // --- Entradas de Stock (Lotes) ---

    @Transactional
    public StockBatchResponse addStockBatch(AddStockBatchRequest request, String username) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductId()));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada con ID: " + request.getLocationId()));

        User user = findUserOrNull(username);

        StockBatch batch = StockBatch.builder()
            .product(product)
            .location(location)
            .quantity(request.getQuantity())
            .expirationDate(request.getExpirationDate())
            .sealedStatus(request.getSealedStatus() != null ? request.getSealedStatus() : SealedStatus.SEALED)
            .batchCode(request.getBatchCode())
            .build();

        StockBatch savedBatch = stockBatchRepository.save(batch);

        // Registrar movimiento inmutable
        InventoryMovement movement = InventoryMovement.builder()
            .product(product)
            .batch(savedBatch)
            .location(location)
            .movementType(MovementType.ENTRY)
            .quantity(request.getQuantity())
            .notes(request.getNotes() != null ? request.getNotes() : "Ingreso manual de stock")
            .user(user)
            .build();
        movementRepository.save(movement);

        // Notificar cambio de nivel total de stock
        Double totalStock = stockBatchRepository.sumQuantityByProductId(product.getId());
        eventPublisher.publishEvent(new StockLevelChangedEvent(product.getId(), totalStock));

        return toStockBatchResponse(savedBatch);
    }

    // --- Consumo de Stock con Algoritmo FEFO ---

    @Transactional
    public ProductStockOverview consumeStock(ConsumeStockRequest request, String username) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductId()));

        Double totalStock = stockBatchRepository.sumQuantityByProductId(product.getId());
        if (totalStock < request.getQuantity()) {
            throw new IllegalArgumentException(String.format(
                "Stock insuficiente para '%s'. Disponible: %.2f, Solicitado: %.2f",
                product.getName(), totalStock, request.getQuantity()));
        }

        User user = findUserOrNull(username);
        double remainingToConsume = request.getQuantity();

        if (request.getBatchId() != null) {
            // Consumo forzado de un lote específico
            StockBatch batch = stockBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado con ID: " + request.getBatchId()));

            if (batch.getQuantity() < remainingToConsume) {
                throw new IllegalArgumentException(String.format(
                    "El lote #%d no tiene cantidad suficiente (Disponible: %.2f, Solicitado: %.2f)",
                    batch.getId(), batch.getQuantity(), remainingToConsume));
            }

            batch.setQuantity(batch.getQuantity() - remainingToConsume);
            stockBatchRepository.save(batch);

            movementRepository.save(InventoryMovement.builder()
                .product(product)
                .batch(batch)
                .location(batch.getLocation())
                .movementType(MovementType.CONSUMPTION)
                .quantity(remainingToConsume)
                .notes(request.getNotes() != null ? request.getNotes() : "Consumo manual de lote")
                .user(user)
                .build());

            eventPublisher.publishEvent(new StockConsumedEvent(product.getId(), batch.getId(), remainingToConsume));
        } else {
            // Algoritmo FEFO: First Expired, First Out
            List<StockBatch> activeBatches = stockBatchRepository.findActiveBatchesFefoOrder(product.getId(), 0.0);

            for (StockBatch batch : activeBatches) {
                if (remainingToConsume <= 0) break;

                double batchQty = batch.getQuantity();
                double toDeduct = Math.min(batchQty, remainingToConsume);

                batch.setQuantity(batchQty - toDeduct);
                stockBatchRepository.save(batch);

                movementRepository.save(InventoryMovement.builder()
                    .product(product)
                    .batch(batch)
                    .location(batch.getLocation())
                    .movementType(MovementType.CONSUMPTION)
                    .quantity(toDeduct)
                    .notes(request.getNotes() != null ? request.getNotes() : "Consumo regular FEFO")
                    .user(user)
                    .build());

                eventPublisher.publishEvent(new StockConsumedEvent(product.getId(), batch.getId(), toDeduct));
                remainingToConsume -= toDeduct;
            }
        }

        Double newTotal = stockBatchRepository.sumQuantityByProductId(product.getId());
        eventPublisher.publishEvent(new StockLevelChangedEvent(product.getId(), newTotal));

        return getProductStockOverview(product.getId());
    }

    // --- Registro de Merma / Desperdicio ---

    @Transactional
    public StockBatchResponse registerWaste(RegisterWasteRequest request, String username) {
        StockBatch batch = stockBatchRepository.findById(request.getBatchId())
            .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado con ID: " + request.getBatchId()));

        if (batch.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(String.format(
                "Cantidad a descartar (%.2f) supera el stock disponible en el lote (%.2f)",
                request.getQuantity(), batch.getQuantity()));
        }

        Product product = batch.getProduct();
        User user = findUserOrNull(username);

        batch.setQuantity(batch.getQuantity() - request.getQuantity());
        StockBatch updatedBatch = stockBatchRepository.save(batch);

        movementRepository.save(InventoryMovement.builder()
            .product(product)
            .batch(batch)
            .location(batch.getLocation())
            .movementType(MovementType.WASTE)
            .quantity(request.getQuantity())
            .wasteReason(request.getWasteReason())
            .notes(request.getNotes())
            .user(user)
            .build());

        Double newTotal = stockBatchRepository.sumQuantityByProductId(product.getId());
        eventPublisher.publishEvent(new StockWastedEvent(product.getId(), batch.getId(), request.getQuantity(), request.getWasteReason()));
        eventPublisher.publishEvent(new StockLevelChangedEvent(product.getId(), newTotal));

        return toStockBatchResponse(updatedBatch);
    }

    // --- Ajuste Físico / Toma de Inventario ---

    @Transactional
    public ProductStockOverview reconcileStock(StockAdjustmentRequest request, String username) {
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductId()));

        Location location = locationRepository.findById(request.getLocationId())
            .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada con ID: " + request.getLocationId()));

        User user = findUserOrNull(username);

        List<StockBatch> locationBatches = stockBatchRepository.findByLocationIdAndQuantityGreaterThan(location.getId(), 0.0).stream()
            .filter(b -> b.getProduct().getId().equals(product.getId()))
            .toList();

        double currentTotalInLocation = locationBatches.stream().mapToDouble(StockBatch::getQuantity).sum();
        double diff = request.getActualQuantity() - currentTotalInLocation;

        if (Math.abs(diff) > 0.0001) {
            if (diff > 0) {
                // Hay más de lo registrado -> Se genera un lote de ajuste positivo
                StockBatch adjustmentBatch = StockBatch.builder()
                    .product(product)
                    .location(location)
                    .quantity(diff)
                    .batchCode("AJUSTE-AUDITORIA")
                    .build();
                stockBatchRepository.save(adjustmentBatch);
            } else {
                // Hay menos de lo registrado -> Se deduce la diferencia de los lotes existentes
                double toReduce = Math.abs(diff);
                for (StockBatch b : locationBatches) {
                    if (toReduce <= 0) break;
                    double deduct = Math.min(b.getQuantity(), toReduce);
                    b.setQuantity(b.getQuantity() - deduct);
                    stockBatchRepository.save(b);
                    toReduce -= deduct;
                }
            }

            movementRepository.save(InventoryMovement.builder()
                .product(product)
                .location(location)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(diff)
                .notes(request.getNotes() != null ? request.getNotes() : "Ajuste de inventario físico")
                .user(user)
                .build());

            Double newTotal = stockBatchRepository.sumQuantityByProductId(product.getId());
            eventPublisher.publishEvent(new StockLevelChangedEvent(product.getId(), newTotal));
        }

        return getProductStockOverview(product.getId());
    }

    // --- Consultas y Métricas de Inventario ---

    @Transactional(readOnly = true)
    public ProductStockOverview getProductStockOverview(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        List<StockBatchResponse> activeBatches = stockBatchRepository.findActiveBatchesFefoOrder(productId, 0.0).stream()
            .map(this::toStockBatchResponse)
            .toList();

        Double total = stockBatchRepository.sumQuantityByProductId(productId);
        boolean isLow = total <= product.getMinStockThreshold();

        return ProductStockOverview.builder()
            .productId(product.getId())
            .productName(product.getName())
            .sku(product.getSku())
            .barCode(product.getBarCode())
            .unitOfMeasure(product.getUnitOfMeasure())
            .totalQuantity(total)
            .minStockThreshold(product.getMinStockThreshold())
            .targetStockQuantity(product.getTargetStockQuantity())
            .isLowStock(isLow)
            .activeBatches(activeBatches)
            .build();
    }

    @Transactional(readOnly = true)
    public List<ProductStockOverview> getAllStockOverview() {
        return productRepository.findAll().stream()
            .map(p -> getProductStockOverview(p.getId()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockBatchResponse> getExpiringBatches(int withinDays) {
        LocalDate limitDate = LocalDate.now().plusDays(withinDays);
        return stockBatchRepository.findByQuantityGreaterThanAndExpirationDateLessThanEqualOrderByExpirationDateAsc(0.0, limitDate)
            .stream()
            .map(this::toStockBatchResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductStockOverview> getLowStockProducts() {
        return getAllStockOverview().stream()
            .filter(ProductStockOverview::isLowStock)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getMovements(Long productId) {
        List<InventoryMovement> movements = productId != null
            ? movementRepository.findByProductIdOrderByCreatedAtDesc(productId)
            : movementRepository.findAll();

        return movements.stream()
            .map(this::toMovementResponse)
            .toList();
    }

    // --- Mappings y Helpers ---

    private User findUserOrNull(String username) {
        if (username == null || username.isBlank()) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private LocationResponse toLocationResponse(Location location) {
        long activeBatches = stockBatchRepository.countByLocationIdAndQuantityGreaterThan(location.getId(), 0.0);
        Double totalQty = stockBatchRepository.sumQuantityByLocationId(location.getId());

        return LocationResponse.builder()
            .id(location.getId())
            .name(location.getName())
            .storageType(location.getStorageType())
            .description(location.getDescription())
            .activeBatchesCount(activeBatches)
            .totalQuantity(totalQty != null ? totalQty : 0.0)
            .createdAt(location.getCreatedAt())
            .build();
    }

    private StockBatchResponse toStockBatchResponse(StockBatch batch) {
        LocalDate now = LocalDate.now();
        boolean expired = batch.getExpirationDate() != null && batch.getExpirationDate().isBefore(now);
        Long daysLeft = batch.getExpirationDate() != null ? ChronoUnit.DAYS.between(now, batch.getExpirationDate()) : null;

        return StockBatchResponse.builder()
            .id(batch.getId())
            .productId(batch.getProduct().getId())
            .productName(batch.getProduct().getName())
            .productSku(batch.getProduct().getSku())
            .productBarCode(batch.getProduct().getBarCode())
            .locationId(batch.getLocation().getId())
            .locationName(batch.getLocation().getName())
            .storageType(batch.getLocation().getStorageType())
            .quantity(batch.getQuantity())
            .expirationDate(batch.getExpirationDate())
            .sealedStatus(batch.getSealedStatus())
            .batchCode(batch.getBatchCode())
            .isExpired(expired)
            .daysUntilExpiration(daysLeft)
            .createdAt(batch.getCreatedAt())
            .build();
    }

    private InventoryMovementResponse toMovementResponse(InventoryMovement movement) {
        return InventoryMovementResponse.builder()
            .id(movement.getId())
            .productId(movement.getProduct().getId())
            .productName(movement.getProduct().getName())
            .batchId(movement.getBatch() != null ? movement.getBatch().getId() : null)
            .locationId(movement.getLocation() != null ? movement.getLocation().getId() : null)
            .locationName(movement.getLocation() != null ? movement.getLocation().getName() : null)
            .movementType(movement.getMovementType())
            .quantity(movement.getQuantity())
            .wasteReason(movement.getWasteReason())
            .notes(movement.getNotes())
            .username(movement.getUser() != null ? movement.getUser().getUsername() : "Sistema")
            .createdAt(movement.getCreatedAt())
            .build();
    }
}
