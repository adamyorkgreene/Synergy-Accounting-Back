package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.config.MailConfig;
import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private MailboxReaderService mailboxReaderService;

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

    public void sendAdminEmail(String to, String from, String subject, String body) {
        String emailPassword = userRepository.findByUsername(from).getEmailPassword();
        JavaMailSender adminMailSender = mailConfig.getAdminMailSender(from.toLowerCase(), emailPassword);
        MimeMessage mm = adminMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mm, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.setFrom(from.toLowerCase() + "@synergyaccounting.app");
            mm.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
            mm.setHeader("X-Mailer", "JavaMailer");
            mm.setHeader("Return-Path", from.toLowerCase() + "@synergyaccounting.app");
            mm.setHeader("Reply-To", from.toLowerCase() + "@synergyaccounting.app");
            mm.setSentDate(new Date());
            adminMailSender.send(mm);
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
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

    public List<AdminEmailObject> getUserEmails(String username) {
        try {
            List<String> rawEmails = mailboxReaderService.getUserEmails(username);
            return mailboxReaderService.parseRawEmailsToObject(rawEmails);
        } catch (IOException | ParseException e) {
            System.err.println(e.getMessage());
            return Collections.emptyList();
        }
    }

    public String simpleConvertTextToHtml(String text) {
        String html = "<html><body>";
        html += "<p>" + text.replaceAll("\n", "<br>") + "</p>";
        html += "</body></html>";
        return html;
    }

    public boolean deleteEmails(AdminEmailObject[] emails) {

        if (emails.length != 0) {

            String userToString = emails[0].getTo().replaceAll("<", "")
                    .replaceAll(">", "").replaceAll("\"", "");
            userToString = userToString.trim();
            String userToString2 = userToString;
            int index = userToString.indexOf(' ');
            if (index != -1) {
                userToString = userToString.substring(0, index);
                userToString2 = userToString2.substring(1, index);
            }
            index = userToString.indexOf('@');
            if (index != -1) {
                userToString = userToString.substring(0, index);
            }
            index = userToString2.indexOf('@');
            if (index != -1) {
                userToString2 = userToString2.substring(0, index);
            }
            System.out.println("User to String: " + userToString);
            System.out.println("User to String 2: " + userToString2);

            User user = userRepository.findByUsername(userToString) != null?
                        userRepository.findByUsername(userToString):
                        userRepository.findByUsername(userToString2);

            if (user == null) {
                System.out.println("User not found.");
                return false;
            }
            System.out.println("User found. Looping through emails.");

            for (AdminEmailObject aeo : emails) {

                String fileName = aeo.getId();
                Path emailFilePath = Paths.get(MailboxReaderService.VMAIL_PATH + user.getUsername().toLowerCase()
                        + "/new/" + fileName);
                System.out.println("Deleting email: " + emailFilePath);

                try {
                    Files.deleteIfExists(emailFilePath);
                    System.out.println("Deleted email: " + fileName);
                } catch (IOException e) {
                    System.err.println("Failed to delete email: " + fileName + " due to: " + e.getMessage());
                    return false;
                }
            }
        }
        return true;
    }
}
