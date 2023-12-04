package ru.tk.ms.sorm.ftp.connector.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ErrorHandlerFlowConfig {

    public static final String ERROR_CHANNEL_ID = "sormErrorChannel";

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlows.from(ERROR_CHANNEL_ID)
                .publishSubscribeChannel(publishSubscribeSpec ->
                        publishSubscribeSpec
                                .subscribe(flow -> flow
                                        .handle(this::handleInboundFlowErrors)))
                .get();
    }

    private void handleInboundFlowErrors(Message<?> message) {
        var errorMessage = (ErrorMessage) message;
        log.error(errorMessage.getPayload().getCause().toString());
    }
}
