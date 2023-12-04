package ru.tk.ms.sorm.ftp.connector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ru.tk.ms.sorm.ftp.connector.gateway.SormFtpGateway;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvroHandleService {

    public static final String SCHEMA_NAME_HEADER = "schema";
    public static final String MESSAGE_ID_HEADER = "messageId";
    public static final String ORIGINAL_MESSAGE_ID_HEADER = "id";
    public static final String MESSAGE_KEY_HEADER = "message";


    private final SormFtpGateway gateway;
    private final MailService mailService;

    private final FileEntityService fileEntityService;

    public void handleAvro(ConsumerRecord<String, GenericRecord> consumerRecord) {

        try {
            var avroDatum = consumerRecord.value().toString();
            var jsonObject = (JSONObject) new JSONObject(consumerRecord.key()).get(MESSAGE_KEY_HEADER);
            var id = Long.parseLong(jsonObject.get(ORIGINAL_MESSAGE_ID_HEADER).toString());
            var avroName = jsonObject.get(SCHEMA_NAME_HEADER).toString();

            fileEntityService.createNewRecord(id);

            gateway.writeAvroDataToCsvFile(new GenericMessage<>(avroDatum,
                    Map.of(SCHEMA_NAME_HEADER, avroName,
                            MESSAGE_ID_HEADER, id)));

        } catch (MessagingException e) {
            String errorMessage = "CSV file creation error: " + e.getMessage();
            log.error(errorMessage);
            mailService.sendMail(errorMessage);
        }

    }

}
