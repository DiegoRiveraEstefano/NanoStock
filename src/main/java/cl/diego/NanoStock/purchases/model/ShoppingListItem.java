package cl.diego.NanoStock.purchases.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cl.diego.NanoStock.catalog.model.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "nanostock_shopping_list_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private ShoppingList shoppingList;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "La cantidad sugerida es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Column(name = "suggested_quantity", nullable = false)
    private Double suggestedQuantity;

    @Column(name = "actual_quantity")
    private Double actualQuantity;

    @Column(name = "is_auto_suggested", nullable = false)
    @Builder.Default
    private boolean isAutoSuggested = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false)
    @Builder.Default
    private ShoppingItemStatus itemStatus = ShoppingItemStatus.PENDING;

    @Column(name = "unit_price_estimate", precision = 12, scale = 2)
    private BigDecimal unitPriceEstimate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.itemStatus == null) {
            this.itemStatus = ShoppingItemStatus.PENDING;
        }
    }
}
