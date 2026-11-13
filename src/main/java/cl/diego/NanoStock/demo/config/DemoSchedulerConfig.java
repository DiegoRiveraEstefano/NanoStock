package cl.diego.NanoStock.demo.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.diego.NanoStock.demo.scheduler.DemoCleanupJob;

@Configuration
@EnableConfigurationProperties(DemoProperties.class)
public class DemoSchedulerConfig {

    @Bean
    public JobDetail demoCleanupJobDetail() {
        return JobBuilder.newJob(DemoCleanupJob.class)
            .withIdentity("demoCleanupJob", "nanostock-demo")
            .withDescription("Verifica cada día si se cumplió el ciclo de 7 días para limpiar datos nuevos en modo demo")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger demoCleanupTrigger(JobDetail demoCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(demoCleanupJobDetail)
            .withIdentity("demoCleanupTrigger", "nanostock-demo")
            .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(3, 30))
            .build();
    }
}
