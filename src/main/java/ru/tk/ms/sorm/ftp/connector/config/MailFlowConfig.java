package ru.tk.ms.sorm.ftp.connector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.integration.support.PropertiesBuilder;

@Configuration
public class MailFlowConfig {

    public final static String SEND_MAIL_CHANNEL_NAME = "sendMailChannel";
    private final static String SEND_MAIL_OUTPUT_ADAPTER_ID = "sendMail";
    private final static String MAIL_SUBJECT = "Sorm error notification";

    @Value("${mail.protocol}")
    private String protocol;

    @Value("${mail.port}")
    private int port;

    @Value("${mail.host}")
    private String host;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.to}")
    private String[] to;

    @Value("${mail.username}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.ssl-enabled}")
    private boolean sslEnabled;

    @Value("${mail.debug-enabled}")
    private String debugEnabled;

    @Value("${mail.tls-enabled}")
    private boolean tlsEnabled;

    @Bean
    public IntegrationFlow sendMailFlow() {
        return IntegrationFlows.from(SEND_MAIL_CHANNEL_NAME)
                .enrichHeaders(Mail.headers()
                        .subjectFunction(message -> MAIL_SUBJECT)
                        .from(from)
                        .toFunction(message -> to))
                .handle(Mail.outboundAdapter(host)
                                .port(port)
                                .credentials(username, password)
                                .protocol(protocol)
                                .javaMailProperties(this::setMailParameters)
                        , e -> e.id(SEND_MAIL_OUTPUT_ADAPTER_ID))
                .get();
    }

    private void setMailParameters(PropertiesBuilder properties) {
        properties.put("mail.debug", debugEnabled);
        if (sslEnabled) {
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.trust", "*");
        }
        if (tlsEnabled) {
            properties.put("mail.smtp.ssl.trust", "*");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        }
    }
}
