package ru.tk.ms.sorm.ftp.connector.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import ru.tk.ms.sorm.ftp.connector.service.AvroHandleService;
import ru.tk.ms.sorm.ftp.connector.service.FileService;

import java.io.File;
import java.util.Objects;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class FileWritingFlowConfig {

    public static final String FILE_WRITING_CHANNEL_NAME = "fileWritingInput";

    private final FileService fileService;

    @Bean
    public IntegrationFlow fileWritingFlow() {
        return IntegrationFlows.from(FILE_WRITING_CHANNEL_NAME)
                .handle(this::createCsvFile)
                .handle(fileService::sendFileToRemoteServer)
                .get();
    }

    private GenericMessage<File> createCsvFile(String data, MessageHeaders headers) {

        var file = fileService.createCsvFile(data,
                Objects.requireNonNull(headers.get(AvroHandleService.SCHEMA_NAME_HEADER)).toString());

        return new GenericMessage<>(file);
    }
}


