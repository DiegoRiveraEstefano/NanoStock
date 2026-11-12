package cl.diego.NanoStock.purchases.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nanostock_stores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del comercio es obligatorio")
    @Column(nullable = false)
    private String name;

    @Column(name = "branch_name")
    private String branchName;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_type")
    @Builder.Default
    private StoreType storeType = StoreType.SUPERMARKET;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Store(String name, String branchName, StoreType storeType) {
        this.name = name;
        this.branchName = branchName;
        this.storeType = storeType != null ? storeType : StoreType.SUPERMARKET;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.storeType == null) {
            this.storeType = StoreType.SUPERMARKET;
        }
    }
}
