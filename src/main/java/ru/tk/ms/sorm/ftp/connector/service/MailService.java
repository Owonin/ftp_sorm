package ru.tk.ms.sorm.ftp.connector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import ru.tk.ms.sorm.ftp.connector.gateway.SormFtpGateway;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final SormFtpGateway gateway;

    public void sendMail(String data) {
        try {
            gateway.sendMail(data);
        } catch (MessagingException e) {
            log.error("Error occur while sending email with message {}", e.getMessage());
        }
    }
}
