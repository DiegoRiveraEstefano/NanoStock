package cl.diego.NanoStock.notifications.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.diego.NanoStock.notifications.job.DailyExpirationCheckJob;
import cl.diego.NanoStock.notifications.job.DailyReplenishmentCheckJob;

@Configuration
public class QuartzSchedulerConfig {

    @Bean
    public JobDetail dailyExpirationJobDetail() {
        return JobBuilder.newJob(DailyExpirationCheckJob.class)
            .withIdentity("dailyExpirationCheckJob", "nanostock-audit")
            .withDescription("Auditoría diaria matutina de caducidad de lotes en inventario")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger dailyExpirationTrigger(JobDetail dailyExpirationJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(dailyExpirationJobDetail)
            .withIdentity("dailyExpirationCheckTrigger", "nanostock-audit")
            .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(7, 0))
            .build();
    }

    @Bean
    public JobDetail dailyReplenishmentJobDetail() {
        return JobBuilder.newJob(DailyReplenishmentCheckJob.class)
            .withIdentity("dailyReplenishmentCheckJob", "nanostock-audit")
            .withDescription("Auditoría diaria matutina de reposición de stock mínimo")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger dailyReplenishmentTrigger(JobDetail dailyReplenishmentJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(dailyReplenishmentJobDetail)
            .withIdentity("dailyReplenishmentCheckTrigger", "nanostock-audit")
            .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(8, 0))
            .build();
    }
}
