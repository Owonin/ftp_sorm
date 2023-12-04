package ru.tk.ms.sorm.ftp.connector.gateway;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import ru.tk.ms.sorm.ftp.connector.config.FileWritingFlowConfig;
import ru.tk.ms.sorm.ftp.connector.config.FtpOutboundFlowConfig;
import ru.tk.ms.sorm.ftp.connector.config.MailFlowConfig;

import java.io.File;

@MessagingGateway
public interface SormFtpGateway {
    @Gateway(requestChannel = FtpOutboundFlowConfig.OUTBOUND_FLOW_CHANNEL_ID)
    void sendToFtp(GenericMessage<File> message) throws MessagingException;

    @Gateway(requestChannel = MailFlowConfig.SEND_MAIL_CHANNEL_NAME)
    void sendMail(String string) throws MessagingException;

    @Gateway(requestChannel = FileWritingFlowConfig.FILE_WRITING_CHANNEL_NAME)
    void writeAvroDataToCsvFile(GenericMessage<String> message) throws MessagingException;
}
