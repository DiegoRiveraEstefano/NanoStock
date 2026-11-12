package cl.diego.NanoStock.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.catalog.dto.BrandRequest;
import cl.diego.NanoStock.catalog.dto.BrandResponse;
import cl.diego.NanoStock.catalog.service.ProductCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalog/brands")
@RequiredArgsConstructor
public class BrandController {

    private final ProductCatalogService catalogService;

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse created = catalogService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        return ResponseEntity.ok(catalogService.getAllBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return catalogService.getAllBrands().stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
