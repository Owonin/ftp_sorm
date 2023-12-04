package ru.tk.ms.sorm.ftp.connector.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.ftp.dsl.Ftp;
import org.springframework.messaging.Message;
import ru.tk.ms.sorm.ftp.connector.service.FileEntityService;
import ru.tk.ms.sorm.ftp.connector.util.FtpSessionFactory;

import java.io.File;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FtpInboundFlowConfig {

    public static final String INBOUND_ADAPTER_CHANNEL_ID = "ftpInboundAdapter";
    private static final String ERROR_CHANNEL_ID = ErrorHandlerFlowConfig.ERROR_CHANNEL_ID;
    private static final String REGEX_FILTER = ".*\\.(bad|bad.log)$";
    private static final String REMOTE_FILENAME_HEADER = "file_remoteFile";
    private static final String REMOTE_TIMESTAMP_HEADER = "timestamp";

    private final FtpSessionFactory factory;
    private final FileEntityService fileEntityService;

    @Value("${ftp.remote-read-directory}")
    private String remoteDirectory;

    @Value("${ftp.local-directory}")
    private String localDirectory;

    @Value("${ftp.users.read-user.username}")
    private String username;

    @Value("${ftp.users.read-user.password}")
    private String password;

    @Value("${ftp.remote-directory-poller-delay}")
    private long delay;


    @Bean
    public IntegrationFlow ftpInboundFlow() {
        var sessionFactory = factory.ftpSessionFactory(username, password);

        return IntegrationFlows
                .from(Ftp.inboundAdapter(sessionFactory)
                                .remoteDirectory(remoteDirectory)
                                .regexFilter(REGEX_FILTER)
                                .localDirectory(new File(localDirectory)),
                        e -> e.id(INBOUND_ADAPTER_CHANNEL_ID)
                                .autoStartup(true)
                                .poller(Pollers.fixedDelay(delay)
                                        .errorChannel(ERROR_CHANNEL_ID)))
                .handle(this::fileHandler)
                .get();
    }

    public void fileHandler(Message<?> message) {
        var messageHeaders = message.getHeaders();

        if (messageHeaders.containsKey(REMOTE_FILENAME_HEADER)) {
            var remoteFileName = (String) messageHeaders.get(REMOTE_FILENAME_HEADER);
            var timestamp = (Long) messageHeaders.get(REMOTE_TIMESTAMP_HEADER);

            if (remoteFileName != null && timestamp != null) {
                var remoteFileExtension = remoteFileName.substring(remoteFileName.lastIndexOf('.'));
                fileEntityService.saveErrorFileToDatabase(remoteFileName, timestamp, remoteFileExtension);
            }
        }
    }
}
