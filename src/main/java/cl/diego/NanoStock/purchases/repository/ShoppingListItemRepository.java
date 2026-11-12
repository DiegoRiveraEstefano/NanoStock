package cl.diego.NanoStock.purchases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.purchases.model.ShoppingListItem;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    List<ShoppingListItem> findByShoppingListId(Long shoppingListId);

    boolean existsByShoppingListIdAndProductId(Long shoppingListId, Long productId);
}
