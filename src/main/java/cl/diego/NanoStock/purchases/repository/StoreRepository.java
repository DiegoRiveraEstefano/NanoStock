package cl.diego.NanoStock.purchases.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.purchases.model.Store;
import cl.diego.NanoStock.purchases.model.StoreType;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByName(String name);

    boolean existsByName(String name);

    List<Store> findByStoreType(StoreType storeType);
}
