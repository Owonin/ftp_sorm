package ru.tk.ms.sorm.ftp.connector.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.integration.ftp.dsl.FtpOutboundGatewaySpec;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;
import ru.tk.ms.sorm.ftp.connector.service.AvroHandleService;
import ru.tk.ms.sorm.ftp.connector.service.FileEntityService;
import ru.tk.ms.sorm.ftp.connector.util.FtpSessionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
//@Configuration
@RequiredArgsConstructor
public class FtpOutboundFlowConfig {
    public static final String OUTBOUND_FLOW_CHANNEL_ID = "toFtpChannel";
    private static final String DELAY_GROUP_ID = "outbound.delay";
    private static final String TIMESTAMP_HEADER = "timestamp";

    private final FtpSessionFactory factory;
    private final FileEntityService fileEntityService;

    @Value("${ftp.remote-write-directory}")
    private String remoteDirectory;

    @Value("${ftp.users.write-user.username}")
    private String username;

    @Value("${ftp.users.write-user.password}")
    private String password;

    @Value("${ftp.validation-check-delay}")
    private long delay;

    @Bean
    public IntegrationFlow ftpOutboundFlow() {
        var sessionFactory = factory.ftpSessionFactory(username, password);

        return IntegrationFlows.from(OUTBOUND_FLOW_CHANNEL_ID)
                .log(LoggingHandler.Level.INFO, "ftpOutboundFlow", m -> "Uploading file "
                        + m.getPayload() + " to " + remoteDirectory)
                .handle(uploadFileToFtp(sessionFactory))
                .delay(DELAY_GROUP_ID,
                        d -> d.defaultDelay(delay))
                .handle(m ->
                        validationCheck(sessionFactory, m)
                )
                .get();
    }

    private FtpOutboundGatewaySpec uploadFileToFtp(SessionFactory<FTPFile> sessionFactory) {
        return Ftp.outboundGateway(sessionFactory, AbstractRemoteFileOutboundGateway.Command.PUT
                        , "payload")
                .remoteDirectoryExpression("'" + remoteDirectory + "/'");
    }

    private void validationCheck(SessionFactory<FTPFile> sessionFactory, Message<?> message) {
        var fileDirectory = (String) message.getPayload();
        var timestamp = (Long) message.getHeaders().get(TIMESTAMP_HEADER);
        var fileName = fileDirectory.substring(fileDirectory.lastIndexOf('/') + 1);
        var id = (Long) (message.getHeaders().get(AvroHandleService.MESSAGE_ID_HEADER));
        var isValidationFailed = true;

        try {
            isValidationFailed = Arrays.stream(sessionFactory
                            .getSession()
                            .listNames(remoteDirectory))
                    .anyMatch(remoteFileName-> remoteFileName.contains(fileName));
        } catch (IOException e) {
            log.info(e.getMessage());
        }

        fileEntityService.createNewFileRecord(fileName, isValidationFailed, timestamp, id);

    }

}
