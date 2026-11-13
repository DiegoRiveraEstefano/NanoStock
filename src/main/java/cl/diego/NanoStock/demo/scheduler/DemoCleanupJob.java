package cl.diego.NanoStock.demo.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import cl.diego.NanoStock.demo.service.DemoResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoCleanupJob implements Job {

    private final DemoResetService demoResetService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            boolean executed = demoResetService.cleanupIfDue();
            log.debug("DemoCleanupJob verificado (limpieza ejecutada: {})", executed);
        } catch (Exception ex) {
            log.error("Error al ejecutar DemoCleanupJob: {}", ex.getMessage(), ex);
            throw new JobExecutionException(ex);
        }
    }
}
