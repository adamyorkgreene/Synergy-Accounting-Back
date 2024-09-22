package edu.kennesaw.appdomain.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendNoReplyEmail(String to, String body) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setFrom("noreply@synergyaccounting.app");
            helper.setTo(to);
            helper.setSubject("Your Verification Code");
            helper.setText(body, true);
            mm.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
            mm.setHeader("X-Mailer", "JavaMailer");
            mm.setHeader("Return-Path", "noreply@synergyaccounting.app");
            mm.setHeader("Reply-To", "support@synergyaccounting.app");
            mm.setHeader("List-Unsubscribe", "<mailto:unsubscribe@synergyaccounting.app>");
            mm.setSentDate(new Date());
            mailSender.send(mm);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}
