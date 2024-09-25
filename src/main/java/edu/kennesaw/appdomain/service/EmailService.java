package edu.kennesaw.appdomain.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendNoReplyEmail(String to, String verificationCode) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Verification Code");
            helper.setText(
                    "<html>" +
                            "<head>" +
                                "<style>" +
                                    "h1 { text-align: center; font-family: 'Copperplate', 'serif'; padding-top: 75px; }" +
                                    "h2 { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "div { text-align: center; }" +
                                "</style>" +
                            "</head>" +
                            "<body>" +
                                "<div>" +
                                    "<img src=\"cid:synergyaccounting\" alt=\"Synergy Accounting\" style=\"height:100px;\" />" +
                                "</div>" +
                                "<h1>" + "Your Verification Code" + "</h1>" +
                                "<h2>" + verificationCode + "</h2>" +
                            "</body>" +
                         "</html>",
                    true);
            sendFormattedEmail(mm, helper);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Password Reset Token");
            helper.setText(
                    "<html>" +
                            "<head>" +
                                "<style>" +
                                    "h1 { text-align: center; font-family: 'Copperplate', 'serif'; padding-top: 75px; }" +
                                    "h2 { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "div { text-align: center; }" +
                                "</style>" +
                            "</head>" +
                            "<body>" +
                                "<div>" +
                                    "<img src=\"cid:synergyaccounting\" alt=\"Synergy Accounting\" style=\"height:100px;\" />" +
                                "</div>" +
                                "<h2>" + "Click this link to reset your password:" + "</h2>" +
                                "<h2>" + resetLink + "</h2>" +
                            "</body>" +
                        "</html>",
                    true);
            sendFormattedEmail(mm, helper);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    private void sendFormattedEmail(MimeMessage mm, MimeMessageHelper helper) throws MessagingException {
        ClassPathResource res = new ClassPathResource("static/images/synergylogo.png");
        helper.setFrom("noreply@synergyaccounting.app");
        helper.addInline("synergyaccounting", res, "image/png");
        mm.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
        mm.setHeader("X-Mailer", "JavaMailer");
        mm.setHeader("Return-Path", "noreply@synergyaccounting.app");
        mm.setHeader("Reply-To", "support@synergyaccounting.app");
        mm.setHeader("List-Unsubscribe", "<mailto:unsubscribe@synergyaccounting.app>");
        mm.setSentDate(new Date());
        mailSender.send(mm);
    }

}
