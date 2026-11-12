package cl.diego.NanoStock.catalog.model;

import java.time.LocalDateTime;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nanostock_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(nullable = false, unique = true)
    private String sku;

    @NotBlank(message = "El código de barras es obligatorio")
    @Column(name = "bar_code", nullable = false)
    private String barCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false)
    @Builder.Default
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UNIT;

    @Column(name = "min_stock_threshold", nullable = false)
    @Builder.Default
    private Double minStockThreshold = 1.0;

    @Column(name = "target_stock_quantity", nullable = false)
    @Builder.Default
    private Double targetStockQuantity = 3.0;

    @Column(name = "is_perishable", nullable = false)
    @Builder.Default
    private boolean isPerishable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Product(String name, String description, String sku, String barCode) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.barCode = barCode;
        this.unitOfMeasure = UnitOfMeasure.UNIT;
        this.minStockThreshold = 1.0;
        this.targetStockQuantity = 3.0;
        this.isPerishable = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.unitOfMeasure == null) {
            this.unitOfMeasure = UnitOfMeasure.UNIT;
        }
        if (this.minStockThreshold == null) {
            this.minStockThreshold = 1.0;
        }
        if (this.targetStockQuantity == null) {
            this.targetStockQuantity = 3.0;
        }
    }
}
