package cl.diego.NanoStock.purchases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.purchases.model.ShoppingList;
import cl.diego.NanoStock.purchases.model.ShoppingListStatus;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    List<ShoppingList> findByStatusOrderByCreatedAtDesc(ShoppingListStatus status);

    Optional<ShoppingList> findFirstByStatusOrderByCreatedAtDesc(ShoppingListStatus status);
}
