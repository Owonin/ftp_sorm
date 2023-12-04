package ru.tk.ms.sorm.ftp.connector.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import ru.tk.ms.sorm.ftp.connector.service.FileEntityService;
import ru.tk.ms.sorm.ftp.connector.service.FileService;
import ru.tk.ms.sorm.ftp.connector.service.MailService;

@Component
@Slf4j
@RequiredArgsConstructor
public class ErrorFilesHandlerJob implements Job {

    private final FileEntityService fileEntityService;
    private final FileService fileService;
    private final MailService mailService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        var fileEntityList = fileEntityService.getRecordsWithErrorFiles();

        fileEntityList.forEach(file -> {
            String errorMessage = "Validation error for file " + file.getCsvFilename()
                    + " with message id "+ file.getId()+"\nFile data: " + fileService.readDataFromErrorFile(file.getErrorFilename())
                    + "\nLog: " + fileService.readDataFromErrorFile(file.getLogFilename());

            log.error(errorMessage);
            mailService.sendMail(errorMessage);

            file.setValidationErrorOccur(false);
            fileEntityService.saveOrUpdateFile(file);
        });
    }
}
