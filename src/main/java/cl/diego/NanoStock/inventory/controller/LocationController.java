package cl.diego.NanoStock.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.inventory.dto.LocationRequest;
import cl.diego.NanoStock.inventory.dto.LocationResponse;
import cl.diego.NanoStock.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory/locations")
@RequiredArgsConstructor
public class LocationController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        LocationResponse created = inventoryService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        return ResponseEntity.ok(inventoryService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable Long id) {
        return inventoryService.getLocationById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
