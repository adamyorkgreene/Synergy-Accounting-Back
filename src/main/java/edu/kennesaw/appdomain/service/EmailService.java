package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.config.MailConfig;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailConfig mailConfig;

    public void sendVerificationEmail(String to, String verifyLink) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Verification Link");
            helper.setText(
                    "<html>" +
                            "<head>" +
                                "<style>" +
                                    "h1 { text-align: center; font-family: 'Copperplate', 'serif'; padding-top: 75px; }" +
                                    "h2 { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "a { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "div { text-align: center; }" +
                                "</style>" +
                            "</head>" +
                            "<body>" +
                                "<div>" +
                                    "<img src=\"cid:synergyaccounting\" alt=\"Synergy Accounting\" style=\"height:100px;\" />" +
                                "</div>" +
                                "<h2>" + "Click here to verify your account:" + "</h2>" +
                                "<a href=\"" + verifyLink + "\">Verify my Account</a>" +
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
                                    "a { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "div { text-align: center; }" +
                                "</style>" +
                            "</head>" +
                            "<body>" +
                                "<div>" +
                                    "<img src=\"cid:synergyaccounting\" alt=\"Synergy Accounting\" style=\"height:100px;\" />" +
                                "</div>" +
                                "<h2>" + "Open this link to reset your password:" + "</h2>" +
                            "<a href=\"" + resetLink + "\">Reset my Password</a>" +
                            "</body>" +
                        "</html>",
                    true);
            sendFormattedEmail(mm, helper);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendAdminConfirmEmail(String to, User user, String confirmationLink) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Synergy Accounting Registration Application: " + user.getFirstName() + " " + user.getLastName());
            helper.setText("The following user has registered for SynergyAccounting and must be approved: \n"
                    + "First Name: " + user.getFirstName() + "\n"
                    + "Last Name: " + user.getLastName() + "\n"
                    + "DOB: " + user.getBirthday().toString() + "\n"
                    + "Email Address: " + user.getEmail() + "\n"
                    + "Home Address: " + user.getAddress() + "\n"
                    + "Please approve verification using this link: " + confirmationLink
            );
            sendFormattedEmail(mm, helper);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendApprovalEmail(String to) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Account has been Approved");
            helper.setText(
                    "<html>" +
                            "<head>" +
                                "<style>" +
                                    "h1 { text-align: center; font-family: 'Copperplate', 'serif'; padding-top: 75px; }" +
                                    "h2 { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "a { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                                    "div { text-align: center; }" +
                                "</style>" +
                            "</head>" +
                            "<body>" +
                                "<div>" +
                                    "<img src=\"cid:synergyaccounting\" alt=\"Synergy Accounting\" style=\"height:100px;\" />" +
                                "</div>" +
                                "<h2><h2/>" +
                                "<h2>" + "Your account is ready to be used!" + "</h2>" +
                                "<h2><h2/>" +
                                "<a href=\"https://synergyaccounting.app/login\">Click here to Login</a>" +
                            "</body>" +
                         "</html>",
                    true);
            sendFormattedEmail(mm, helper);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendPasswordExpirationNotification(User to, Date expirationDate) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to.getEmail());
            helper.setSubject("Your Password is about to Expire!");
            helper.setText(
                    "<html>" +
                            "<head>" +
                            "<style>" +
                            "h1 { text-align: center; font-family: 'Copperplate', 'serif'; padding-top: 75px; }" +
                            "h2 { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                            "a { text-align: center; font-family: 'Copperplate', 'serif'; }" +
                            "div { text-align: center; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div>" +
                            "<img src=\"cid:synergyaccounting\" alt=\"Synergy Accounting\" style=\"height:100px;\" />" +
                            "</div>" +
                            "<h2><h2/>" +
                            "<h2>" + "Your password will expire on: " + expirationDate.toString() + "</h2>" +
                            "<h2><h2/>" +
                            "<a href=\"https://synergyaccounting.app/login\">Reset your password now!</a>" +
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

    public void sendAdminEmail(String to, User from, String subject, String body) {
        JavaMailSender adminMailSender = mailConfig.getAdminMailSender(from.getUsername().toLowerCase(), from.getEmailPassword());
        MimeMessage mm = adminMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.setFrom(from.getUsername().toLowerCase() + "@synergyaccounting.app");
            mm.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
            mm.setHeader("X-Mailer", "JavaMailer");
            mm.setHeader("Return-Path", from.getUsername().toLowerCase() + "@synergyaccounting.app");
            mm.setHeader("Reply-To", from.getUsername().toLowerCase() + "@synergyaccounting.app");
            mm.setSentDate(new Date());
            adminMailSender.send(mm);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            log.error("e: ", e);
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendPasswordExpirationNotifications() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        cal.add(Calendar.DAY_OF_YEAR, -90);

        Date before = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 3);

        Date after = cal.getTime();

        List<User> usersWithExpiringPasswords = userRepository.findAllByLastPasswordResetIsBetween(before, after);

        for (int i = 0; i < usersWithExpiringPasswords.size(); i += 1) {
            int end = Math.min(usersWithExpiringPasswords.size(), i + 1);
            List<User> batch = usersWithExpiringPasswords.subList(i, end);
            for (User user : batch) {
                cal.setTime(user.getLastPasswordReset());
                cal.add(Calendar.DAY_OF_YEAR, 90);
                Date expirationDate = cal.getTime();
                sendPasswordExpirationNotification(user, expirationDate);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
