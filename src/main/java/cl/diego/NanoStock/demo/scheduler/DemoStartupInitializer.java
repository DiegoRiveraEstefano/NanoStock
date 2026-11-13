package cl.diego.NanoStock.demo.scheduler;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import cl.diego.NanoStock.demo.config.DemoProperties;
import cl.diego.NanoStock.demo.service.DemoDataSeeder;
import cl.diego.NanoStock.demo.service.DemoResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoStartupInitializer implements ApplicationRunner {

    private final DemoProperties demoProperties;
    private final DemoDataSeeder demoDataSeeder;
    private final DemoResetService demoResetService;

    @Override
    public void run(ApplicationArguments args) {
        if (!demoProperties.isEnabled()) {
            return;
        }

        log.info("Inicializando Modo Demo de Portafolio (reinicio de datos nuevos cada {} días)...",
            demoProperties.getResetIntervalDays());

        if (demoProperties.isSeedOnStartup()) {
            demoDataSeeder.ensureSeedData();
        }

        // Si el servidor estuvo apagado al cumplirse los 7 días, ejecuta limpieza pendiente al arrancar
        demoResetService.cleanupIfDue();
    }
}
