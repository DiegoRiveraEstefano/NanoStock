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
public class DailyExpirationCheckJob implements Job {

    private final NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Ejecutando Job programado [DailyExpirationCheckJob]...");
        try {
            int detected = notificationService.runExpirationCheck(7);
            log.info("DailyExpirationCheckJob finalizado con éxito (detectados: {})", detected);
        } catch (Exception ex) {
            log.error("Error al ejecutar DailyExpirationCheckJob: {}", ex.getMessage(), ex);
            throw new JobExecutionException(ex);
        }
    }
}
