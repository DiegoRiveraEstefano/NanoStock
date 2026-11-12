package cl.diego.NanoStock.purchases.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.purchases.model.Receipt;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> findByPurchaseDateBetweenOrderByPurchaseDateDesc(LocalDate start, LocalDate end);

    List<Receipt> findByStoreIdOrderByPurchaseDateDesc(Long storeId);

    List<Receipt> findAllByOrderByPurchaseDateDesc();
}
