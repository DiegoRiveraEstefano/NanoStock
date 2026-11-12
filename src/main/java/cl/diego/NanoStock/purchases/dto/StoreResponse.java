package cl.diego.NanoStock.purchases.dto;

import java.time.LocalDateTime;

import cl.diego.NanoStock.purchases.model.StoreType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreResponse {

    private Long id;
    private String name;
    private String branchName;
    private StoreType storeType;
    private String address;
    private LocalDateTime createdAt;
}
