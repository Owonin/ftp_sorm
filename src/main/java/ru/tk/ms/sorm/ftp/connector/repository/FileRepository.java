package ru.tk.ms.sorm.ftp.connector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.tk.ms.sorm.ftp.connector.entity.FileEntity;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findFileEntityByCsvFilename(String filename);

    @Query(value = "SELECT * FROM FILES as f " +
            "WHERE f.ERROR_FILENAME is NOT null " +
            "AND f.LOG_FILENAME is NOT null " +
            "AND f.IS_VALIDATION_ERROR_OCCUR IS true", nativeQuery = true)
    List<FileEntity> findRecordsWithErrorFiles();

}
