package ru.tk.ms.sorm.ftp.connector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tk.ms.sorm.ftp.connector.entity.FileEntity;
import ru.tk.ms.sorm.ftp.connector.exception.FileNotFoundException;
import ru.tk.ms.sorm.ftp.connector.repository.FileRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileEntityService {

    private static final String ERROR_FILE_EXTENSION = ".bad";
    private static final String ERROR_LOG_EXTENSION = ".log";
    private static final String CSV_FILE_EXTENSION = ".csv";

    @Value("${ftp.error-file-receiving-timeout}")
    private long timeout;

    private final FileRepository fileRepository;
    private final MailService mailService;

    public void saveOrUpdateFile(FileEntity file) {
        fileRepository.save(file);
    }

    public FileEntity findFileByCsvFilename(String filename) {
        return fileRepository.findFileEntityByCsvFilename(filename)
                .orElseThrow(() -> new FileNotFoundException("Csv file " + filename + " has not been found"));
    }

    public List<FileEntity> getRecordsWithErrorFiles() {
        return fileRepository.findRecordsWithErrorFiles();
    }

    public void createNewFileRecord(String fileName, boolean validationFailed, Long timestamp, Long id) {
        var fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Record with id " + id + " has not been found"));

        fileEntity.setCsvFilename(fileName);
        fileEntity.setCsvFileSendTimestamp(timestamp);
        fileEntity.setValidationErrorOccur(validationFailed);

        if (validationFailed) {
            var errorMessage = "File " + fileName + " has not been taken by validator";
            log.warn(errorMessage);
            mailService.sendMail(errorMessage);
        } else {
            log.info("File {} has been taken by validator", fileName);
        }

        saveOrUpdateFile(fileEntity);
    }

    public void saveErrorFileToDatabase(String errorFilename, long timestamp, String remoteFileExtension) {
        var csvFilename = errorFilename.substring(0, errorFilename.indexOf(CSV_FILE_EXTENSION) + CSV_FILE_EXTENSION.length());
        var fileEntity = findFileByCsvFilename(csvFilename);

        if (remoteFileExtension.equals(ERROR_FILE_EXTENSION)) {
            fileEntity.setErrorFilename(errorFilename);
            fileEntity.setErrorFileReceivedTimestamp(timestamp);
            fileEntity.setValidationErrorOccur(true);
            saveOrUpdateFile(fileEntity);
        }

        if (remoteFileExtension.equals(ERROR_LOG_EXTENSION)) {
            fileEntity.setLogFilename(errorFilename);
            fileEntity.setLogReceivedTimestamp(timestamp);
            fileEntity.setValidationErrorOccur(true);
            saveOrUpdateFile(fileEntity);
        }
    }

    public void createNewRecord(Long id) {
        var fileEntity = new FileEntity();
        fileEntity.setId(id);

        fileRepository.save(fileEntity);
    }
}
