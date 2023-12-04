package ru.tk.ms.sorm.ftp.connector.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FtpSessionFactory {

    @Value("${ftp.host}")
    private String host;

    @Value("${ftp.port}")
    private int port;

    public SessionFactory<FTPFile> ftpSessionFactory(String username, String password) {
        var sessionFactory = new DefaultFtpSessionFactory();
        sessionFactory.setHost(host);
        sessionFactory.setPort(port);
        sessionFactory.setUsername(username);
        sessionFactory.setPassword(password);

        return new CachingSessionFactory<>(sessionFactory);
    }

}
