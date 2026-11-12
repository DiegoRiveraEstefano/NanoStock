package cl.diego.NanoStock.purchases.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import cl.diego.NanoStock.purchases.dto.AddShoppingItemRequest;
import cl.diego.NanoStock.purchases.dto.CreateShoppingListRequest;
import cl.diego.NanoStock.purchases.dto.ShoppingListItemResponse;
import cl.diego.NanoStock.purchases.dto.ShoppingListResponse;
import cl.diego.NanoStock.purchases.dto.UpdateShoppingItemStatusRequest;
import cl.diego.NanoStock.purchases.model.ShoppingItemStatus;
import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.model.ShoppingListItem;
import cl.diego.NanoStock.purchases.model.ShoppingListStatus;
import cl.diego.NanoStock.purchases.model.Store;
import cl.diego.NanoStock.purchases.repository.ShoppingListItemRepository;
import cl.diego.NanoStock.purchases.repository.ShoppingListRepository;
import cl.diego.NanoStock.purchases.repository.StoreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final ProductRepository productRepository;
    private final StockBatchRepository stockBatchRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ShoppingListResponse createList(CreateShoppingListRequest request) {
        Store store = null;
        if (request.getTargetStoreId() != null) {
            store = storeRepository.findById(request.getTargetStoreId()).orElse(null);
        }

        ShoppingList list = ShoppingList.builder()
            .title(request.getTitle().trim())
            .targetStore(store)
            .status(ShoppingListStatus.ACTIVE)
            .build();

        return toShoppingListResponse(shoppingListRepository.save(list));
    }

    @Transactional
    public ShoppingList getOrCreateActiveList() {
        return shoppingListRepository.findFirstByStatusOrderByCreatedAtDesc(ShoppingListStatus.ACTIVE)
            .orElseGet(() -> shoppingListRepository.save(
                ShoppingList.builder()
                    .title("Lista de Compras del Hogar")
                    .status(ShoppingListStatus.ACTIVE)
                    .build()
            ));
    }

    @Transactional
    public ShoppingListResponse generateSuggestedItems(Long listId) {
        ShoppingList list = listId != null
            ? shoppingListRepository.findById(listId).orElseThrow(() -> new IllegalArgumentException("Lista no encontrada"))
            : getOrCreateActiveList();

        List<Product> allProducts = productRepository.findAll();

        for (Product product : allProducts) {
            Double currentStock = stockBatchRepository.sumQuantityByProductId(product.getId());
            if (currentStock <= product.getMinStockThreshold()) {
                double needed = Math.max(1.0, product.getTargetStockQuantity() - currentStock);

                boolean alreadyInList = shoppingListItemRepository.existsByShoppingListIdAndProductId(list.getId(), product.getId());
                if (!alreadyInList) {
                    ShoppingListItem item = ShoppingListItem.builder()
                        .product(product)
                        .suggestedQuantity(needed)
                        .actualQuantity(needed)
                        .isAutoSuggested(true)
                        .itemStatus(ShoppingItemStatus.PENDING)
                        .build();
                    list.addItem(item);
                }
            }
        }

        return toShoppingListResponse(shoppingListRepository.save(list));
    }

    @Transactional
    public ShoppingListItemResponse addItem(Long listId, AddShoppingItemRequest request) {
        ShoppingList list = shoppingListRepository.findById(listId)
            .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada con ID: " + listId));

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductId()));

        ShoppingListItem item = ShoppingListItem.builder()
            .product(product)
            .suggestedQuantity(request.getQuantity())
            .actualQuantity(request.getQuantity())
            .isAutoSuggested(false)
            .itemStatus(ShoppingItemStatus.PENDING)
            .unitPriceEstimate(request.getUnitPriceEstimate())
            .build();

        list.addItem(item);
        ShoppingListItem saved = shoppingListItemRepository.save(item);
        return toShoppingListItemResponse(saved);
    }

    @Transactional
    public ShoppingListItemResponse updateItemStatus(Long itemId, UpdateShoppingItemStatusRequest request) {
        ShoppingListItem item = shoppingListItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado con ID: " + itemId));

        item.setItemStatus(request.getItemStatus());
        if (request.getActualQuantity() != null) {
            item.setActualQuantity(request.getActualQuantity());
        }

        return toShoppingListItemResponse(shoppingListItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (!shoppingListItemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Ítem no encontrado con ID: " + itemId);
        }
        shoppingListItemRepository.deleteById(itemId);
    }

    @Transactional
    public ShoppingListResponse completeList(Long listId) {
        ShoppingList list = shoppingListRepository.findById(listId)
            .orElseThrow(() -> new IllegalArgumentException("Lista no encontrada con ID: " + listId));

        list.setStatus(ShoppingListStatus.COMPLETED);
        return toShoppingListResponse(shoppingListRepository.save(list));
    }

    @Transactional(readOnly = true)
    public Optional<ShoppingListResponse> getListById(Long listId) {
        return shoppingListRepository.findById(listId).map(this::toShoppingListResponse);
    }

    @Transactional(readOnly = true)
    public List<ShoppingListResponse> getAllLists() {
        return shoppingListRepository.findAll().stream()
            .map(this::toShoppingListResponse)
            .toList();
    }

    public ShoppingListResponse toShoppingListResponse(ShoppingList list) {
        List<ShoppingListItemResponse> items = list.getItems().stream()
            .map(this::toShoppingListItemResponse)
            .toList();

        return ShoppingListResponse.builder()
            .id(list.getId())
            .title(list.getTitle())
            .status(list.getStatus())
            .targetStoreId(list.getTargetStore() != null ? list.getTargetStore().getId() : null)
            .targetStoreName(list.getTargetStore() != null ? list.getTargetStore().getName() : null)
            .items(items)
            .createdAt(list.getCreatedAt())
            .build();
    }

    public ShoppingListItemResponse toShoppingListItemResponse(ShoppingListItem item) {
        return ShoppingListItemResponse.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .productBarCode(item.getProduct().getBarCode())
            .suggestedQuantity(item.getSuggestedQuantity())
            .actualQuantity(item.getActualQuantity())
            .isAutoSuggested(item.isAutoSuggested())
            .itemStatus(item.getItemStatus())
            .unitPriceEstimate(item.getUnitPriceEstimate())
            .createdAt(item.getCreatedAt())
            .build();
    }
}
