package ru.tk.ms.sorm.ftp.connector.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.tk.ms.sorm.ftp.connector.service.AvroHandleService;


@Slf4j
//@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final AvroHandleService avroHandleService;

    @KafkaListener(topics = {"${spring.kafka.listener.topics.sorm-clients}",
            "${spring.kafka.listener.topics.sorm-common-payments}"})
    public void sormTopicsListener(ConsumerRecord<String, GenericRecord> consumerRecord) {

        log.info("ConsumerRecord offset: {}, consumerRecord value: {} ", consumerRecord.offset(), (consumerRecord.value()));

        avroHandleService.handleAvro(consumerRecord);
    }
}
