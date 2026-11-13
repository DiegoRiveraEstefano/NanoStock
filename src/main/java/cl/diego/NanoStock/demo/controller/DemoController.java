package cl.diego.NanoStock.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.demo.dto.DemoStatusDto;
import cl.diego.NanoStock.demo.service.DemoResetService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoResetService demoResetService;

    @GetMapping("/status")
    public ResponseEntity<DemoStatusDto> getStatus() {
        return ResponseEntity.ok(demoResetService.getStatus());
    }

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DemoStatusDto> triggerReset() {
        return ResponseEntity.ok(demoResetService.cleanupNewDataAndReseed());
    }
}
