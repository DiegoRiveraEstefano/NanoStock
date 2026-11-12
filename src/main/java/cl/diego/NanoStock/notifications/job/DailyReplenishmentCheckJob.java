package cl.diego.NanoStock.notifications.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import cl.diego.NanoStock.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReplenishmentCheckJob implements Job {

    private final NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Ejecutando Job programado [DailyReplenishmentCheckJob]...");
        try {
            int replenishments = notificationService.runReplenishmentCheck();
            log.info("DailyReplenishmentCheckJob finalizado con éxito (artículos sugeridos: {})", replenishments);
        } catch (Exception ex) {
            log.error("Error al ejecutar DailyReplenishmentCheckJob: {}", ex.getMessage(), ex);
            throw new JobExecutionException(ex);
        }
    }
}
