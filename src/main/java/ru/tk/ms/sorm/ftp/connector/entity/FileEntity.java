package ru.tk.ms.sorm.ftp.connector.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "FILES")
@Getter
@Setter
public class FileEntity {
    @Id
    private Long id;

    @Column(name = "CSV_FILENAME")
    private String csvFilename;

    @Column(name = "ERROR_FILENAME")
    private String errorFilename;

    @Column(name = "LOG_FILENAME")
    private String logFilename;

    @Column(name = "CSV_FILE_SEND_TIMESTAMP")
    private Long csvFileSendTimestamp;

    @Column(name = "ERROR_FILE_RECEIVED_TIMESTAMP")
    private Long errorFileReceivedTimestamp;

    @Column(name = "LOG_FILE_RECEIVED_TIMESTAMP")
    private Long logReceivedTimestamp;

    @Column(name = "IS_VALIDATION_ERROR_OCCUR")
    private boolean isValidationErrorOccur;

}
