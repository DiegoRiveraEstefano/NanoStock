package cl.diego.NanoStock.purchases.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.diego.NanoStock.purchases.model.ReceiptItem;

@Repository
public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {

    List<ReceiptItem> findByProductId(Long productId);

    List<ReceiptItem> findByReceiptId(Long receiptId);
}
