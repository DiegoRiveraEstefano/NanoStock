package cl.diego.NanoStock.inventory.dto;

import cl.diego.NanoStock.inventory.model.StorageType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {

    @NotBlank(message = "El nombre del lugar de almacenamiento es obligatorio")
    private String name;

    private StorageType storageType;

    private String description;
}
