package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.config.MailConfig;
import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.dto.EmailAttachment;
import edu.kennesaw.appdomain.dto.ReadResponseDTO;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.UserDate;
import edu.kennesaw.appdomain.repository.UserDateRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.types.UserType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
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
    private UserDateRepository userDateRepository;

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
                    + "DOB: " + user.getUserDate().getBirthday().toString() + "\n"
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

    public void sendBasicNoReplyEmail(String to, String subject, String body) {
        MimeMessage mm = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mmh = new MimeMessageHelper(mm, true, "UTF-8");
            mmh.setTo(to);
            mmh.setFrom("noreply@synergyaccounting.app");
            mmh.setSubject(subject);
            mmh.setText(body, false);
            mm.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
            mm.setHeader("X-Mailer", "JavaMailer");
            mm.setHeader("Return-Path", "noreply@synergyaccounting.app");
            mm.setHeader("Reply-To", "support@synergyaccounting.app");
            mm.setHeader("List-Unsubscribe", "<mailto:unsubscribe@synergyaccounting.app>");
            mm.setSentDate(new Date());
            mailSender.send(mm);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAdminEmail(String to, String from, String subject, String body, List<EmailAttachment> attachments) {
        // Retrieve email password for the sender
        String emailPassword = userRepository.findByUsername(from)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserSecurity()
                .getEmailPassword();

        JavaMailSender adminMailSender = mailConfig.getAdminMailSender(from.toLowerCase(), emailPassword);
        MimeMessage mimeMessage = adminMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // Enable multipart emails
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // Send text as plain text
            helper.setFrom(from.toLowerCase() + "@synergyaccounting.app");

            // Add attachments if present
            if (attachments != null && !attachments.isEmpty()) {
                for (EmailAttachment attachment : attachments) {
                    if (attachment.getFileName() != null && attachment.getContentBase64() != null) {
                        try {
                            byte[] content = Base64.getDecoder().decode(attachment.getContentBase64());
                            helper.addAttachment(attachment.getFileName(), new ByteArrayResource(content));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Failed to decode attachment: " + attachment.getFileName());
                            throw new RuntimeException("Invalid attachment encoding for file: " + attachment.getFileName(), e);
                        }
                    } else {
                        System.err.println("Attachment is missing fileName or contentBase64: " + attachment);
                    }
                }
            }

            mimeMessage.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
            mimeMessage.setHeader("X-Mailer", "JavaMailer");
            mimeMessage.setHeader("Return-Path", from.toLowerCase() + "@synergyaccounting.app");
            mimeMessage.setHeader("Reply-To", from.toLowerCase() + "@synergyaccounting.app");
            mimeMessage.setSentDate(new Date());

            adminMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Error creating or sending email: " + e.getMessage());
            throw new RuntimeException("Error sending email", e);
        }
    }


    public void sendMassManagerEmail(String subject, String body) {
        List<User> managers = userRepository.getAllUsersByUserType(UserType.MANAGER);
        List<User> admins = userRepository.getAllUsersByUserType(UserType.ADMINISTRATOR);
        managers.addAll(admins);
        for (User user : managers) {
            MimeMessage mm = mailSender.createMimeMessage();
            try {
                MimeMessageHelper mmh = new MimeMessageHelper(mm, true, "UTF-8");
                mmh.setTo(user.getUsername() + "@synergyaccounting.app");
                mmh.setFrom("noreply@synergyaccounting.app");
                mmh.setSubject(subject);
                mmh.setText(body, false);
                mm.setHeader("Message-ID", "<" + System.currentTimeMillis() + "@synergyaccounting.app>");
                mm.setHeader("X-Mailer", "JavaMailer");
                mm.setHeader("Return-Path", "noreply@synergyaccounting.app");
                mm.setHeader("Reply-To", "support@synergyaccounting.app");
                mm.setHeader("List-Unsubscribe", "<mailto:unsubscribe@synergyaccounting.app>");
                mm.setSentDate(new Date());
                mailSender.send(mm);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
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

        List<UserDate> userDatesWithExpiringPasswords = userDateRepository.findUserDatesByLastPasswordResetBetween
                (before, after);

        List<User> usersWithExpiringPasswords = userRepository.findByUserDateIn(userDatesWithExpiringPasswords);

        for (int i = 0; i < usersWithExpiringPasswords.size(); i += 1) {
            int end = Math.min(usersWithExpiringPasswords.size(), i + 1);
            List<User> batch = usersWithExpiringPasswords.subList(i, end);
            for (User user : batch) {
                cal.setTime(user.getUserDate().getLastPasswordReset());
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
            return mailboxReaderService.getUserEmails(username);
        } catch (IOException e) {
            System.err.println("Error fetching emails: " + e.getMessage());
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

            User user = userRepository.findByUsername(userToString).isPresent() ?
                        userRepository.findByUsername(userToString).get():
                        userRepository.findByUsername(userToString2).isPresent() ?
                        userRepository.findByUsername(userToString2).get():
                        null;

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

    public ResponseEntity<List<String>> getAllAccountantEmails() {
        List<String> emails = new ArrayList<>();
        userRepository.getAllUsersByUserType(UserType.ACCOUNTANT).forEach(user -> {
            emails.add(user.getUsername() + "@synergyaccounting.app");
        });
        return ResponseEntity.ok(emails);
    }

    public ResponseEntity<List<String>> getAllManagerEmails() {
        List<String> emails = new ArrayList<>();
        userRepository.getAllUsersByUserType(UserType.MANAGER).forEach(user -> {
            emails.add(user.getUsername() + "@synergyaccounting.app");
        });
        return ResponseEntity.ok(emails);
    }

}
