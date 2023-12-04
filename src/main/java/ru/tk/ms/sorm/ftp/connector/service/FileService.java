package ru.tk.ms.sorm.ftp.connector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.util.PoolItemNotAvailableException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ru.tk.ms.sorm.ftp.connector.gateway.SormFtpGateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String CSV_FILENAME_TIME_PATTERN = "yyyyMMddHHmmss";
    private static final String EMPTY_AVRO_ERROR_MESSAGE = "Avro message is empty";

    @Value("${ftp.local-directory}")
    private String localDirectory;

    private final MailService mailService;
    private final SormFtpGateway gateway;

    public String readDataFromErrorFile(String fileName) {

        try {
            var fileInputStream = new FileInputStream(localDirectory + "/" + fileName);
            var data = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);

            if (data.isBlank()) {
                log.info("Error log file {} is empty", fileName);
            } else {
                return data;
            }

        } catch (IOException e) {
            log.error("Error occur while reading file {}", fileName);
        }

        return "";
    }

    public File createCsvFile(String jsonString, String avroName) {

        var regex = "([A-Z][a-z]+)";
        var replacement = "$1_";

        avroName = avroName
                .replaceAll(regex, replacement)
                .toUpperCase();

        var currentTime = new SimpleDateFormat(CSV_FILENAME_TIME_PATTERN)
                .format(Calendar
                        .getInstance()
                        .getTime());

        var fileName = avroName + currentTime + CSV_FILE_EXTENSION;

        log.info("Creating new csv file {}", fileName);
        var file = new File(localDirectory +"/"+ fileName);

        try {
            writeDataToCsvFile(jsonString, file);
        } catch (NoSuchElementException e) {
            log.warn(EMPTY_AVRO_ERROR_MESSAGE);
            deleteFile(file);
        } catch (Exception e) {
            log.error(e.getMessage());
            mailService.sendMail(e.getMessage());
            deleteFile(file);
        }

        return file;
    }

    private void deleteFile(File file) {
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));
            log.info("File {} has been deleted", file.getName());
        } catch (IOException exception) {
            log.error("File {} has not been deleted with error {}", file.getName(), exception.getMessage());
        }
    }


    private void writeDataToCsvFile(String datumString, File csvFile) throws IOException {
        var jsonTree = new ObjectMapper().readTree(datumString).elements().next();
        var csvSchemaBuilder = CsvSchema.builder();
        var firstJsonNode = jsonTree.elements().next();


        firstJsonNode.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);

        var csvSchema = csvSchemaBuilder.build();
        var csvMapper = new CsvMapper();

        csvMapper.writerFor(JsonNode.class)
                .with(csvSchema)
                .writeValue(csvFile, jsonTree);
    }

    public void sendFileToRemoteServer(Message<?> message) {

        try {
            gateway.sendToFtp(new GenericMessage<>((File) message.getPayload(),
                    Map.of(AvroHandleService.MESSAGE_ID_HEADER,
                            Objects.requireNonNull(message.getHeaders().get(AvroHandleService.MESSAGE_ID_HEADER)))));
        } catch (MessageHandlingException e) {
            if (e.getCause() instanceof PoolItemNotAvailableException) {
                log.error("Connection timed out");
                sendFileToRemoteServer(message);
            } else {
                log.error("Error occur during uploading file to FTP server with message {}", e.getMessage());
            }
        }
    }

}
