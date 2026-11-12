package cl.diego.NanoStock.purchases.dto;

import cl.diego.NanoStock.purchases.model.StoreType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {

    @NotBlank(message = "El nombre del comercio es obligatorio")
    private String name;

    private String branchName;

    private StoreType storeType;

    private String address;
}
