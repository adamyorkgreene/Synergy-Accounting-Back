package edu.kennesaw.appdomain.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Session;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${MAILPW}")
    private String mailPassword;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("mail.synergyaccounting.app");
        mailSender.setPort(587);
        mailSender.setUsername("noreply@synergyaccounting.app");

        if (mailPassword == null || mailPassword.isEmpty()) {
            throw new IllegalStateException("No password specified for the email sender");
        }
        mailSender.setPassword(mailPassword);
        mailSender.setProtocol("smtp");
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "mail.synergyaccounting.app");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return mailSender;

    }

    public JavaMailSender getAdminMailSender(String username, String password) {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("mail.synergyaccounting.app");
        mailSender.setPort(587);
        mailSender.setUsername(username.toLowerCase() + "@synergyaccounting.app");

        mailSender.setPassword(password);
        mailSender.setProtocol("smtp");
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "mail.synergyaccounting.app");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return mailSender;
    }

}
