package es.codeurjc.exercise4you.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@ConfigurationProperties
public class EmailConfig {
    private static final int GMAIL_SMTP_PORT = 587;
    
    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String user;

    @Value("${spring.mail.password}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.host", host);
        props.put("port", GMAIL_SMTP_PORT);
        props.put("mail.username", user);
        props.put("mail.password", password);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        mailSender.setJavaMailProperties(props);

        mailSender.setHost(host);
        mailSender.setUsername(user);
        mailSender.setPassword(password);
        mailSender.setPort(GMAIL_SMTP_PORT);
        mailSender.setProtocol("smtp");

        return mailSender;
    }
}
