package ru.tk.ms.sorm.ftp.connector.bootstrap;


import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ru.tk.ms.sorm.ftp.connector.entity.FileEntity;
import ru.tk.ms.sorm.ftp.connector.repository.FileRepository;
import ru.tk.ms.sorm.ftp.connector.service.SchedulerService;

@Component
@RequiredArgsConstructor
public class Bootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private final SchedulerService schedulerService;
    private final FileRepository fileRepository;

    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent contextRefreshedEvent) {
        schedulerService.schedule();
        FileEntity entity1 = new FileEntity();
        entity1.setId(1L);
        entity1.setCsvFilename("SORM_COMMON_PAYMENTS_20220728163559.csv");
        fileRepository.save(entity1);

    }
}
