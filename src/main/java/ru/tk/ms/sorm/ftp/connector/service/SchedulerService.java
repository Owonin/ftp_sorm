package ru.tk.ms.sorm.ftp.connector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tk.ms.sorm.ftp.connector.job.ErrorFilesHandlerJob;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {

    private final Scheduler scheduler;

    @Value("${spring.quartz.job-interval}")
    private long interval;

    public void schedule() {

        var jobName = ErrorFilesHandlerJob.class.getSimpleName();

        var scheduleTimer = getSimpleScheduleBuilder();

        var job = getJobDetail(jobName);

        var trigger = getTrigger(jobName, scheduleTimer);

        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error(e.getMessage());
        }

    }

    private Trigger getTrigger(String jobName, SimpleScheduleBuilder scheduleTimer) {
        return TriggerBuilder
                .newTrigger()
                .forJob(jobName)
                .startNow()
                .withSchedule(scheduleTimer)
                .build();
    }

    private JobDetail getJobDetail(String jobName) {
        return JobBuilder
                .newJob(ErrorFilesHandlerJob.class)
                .withIdentity(jobName)
                .build();
    }

    private SimpleScheduleBuilder getSimpleScheduleBuilder() {
        return SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMilliseconds(interval)
                .repeatForever();
    }

}
