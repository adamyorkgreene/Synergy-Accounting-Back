package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.dto.EmailAttachment;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MailboxReaderService {

    public static final String VMAIL_PATH = "/var/vmail/synergyaccounting.app/";

    public List<String> getUserEmails(String username) throws IOException {

        List<String> emails = new ArrayList<>();
        Path userMailDir = Paths.get(VMAIL_PATH + username.toLowerCase() + "/new");

        if (Files.exists(userMailDir) && Files.isDirectory(userMailDir)) {
            try (DirectoryStream<Path> userMails = Files.newDirectoryStream(userMailDir)) {
                for (Path userMail : userMails) {
                    StringBuilder emailContent = new StringBuilder();

                    try (BufferedReader reader = Files.newBufferedReader(userMail)) {
                        emailContent.append(userMail.getFileName().toString()).append("\n");
                        String line = reader.readLine();
                        while (line != null) {
                            emailContent.append(line).append("\n");
                            line = reader.readLine();

                        }

                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }

                    emails.add(emailContent.toString());

                }

            } catch (Exception e){
                System.err.println(e.getMessage());
            }

        }
        return emails;
    }

    public List<String> parseRawEmailsToHTML(List<String> rawEmails) throws IOException {

        List<String> htmlEmails = new ArrayList<>();

        for (String rawEmail : rawEmails) {
            htmlEmails.add(parseRawEmailToHTML(rawEmail));
        }

        return htmlEmails;

    }

    public List<AdminEmailObject> parseRawEmailsToObject(List<String> rawEmails) throws IOException, ParseException, MessagingException {

        List<AdminEmailObject> objectEmails = new ArrayList<>();

        for (String rawEmail : rawEmails) {
            objectEmails.add(parseEmail(rawEmail));
        }

        return objectEmails;

    }

    private String parseRawEmailToHTML(String rawEmail) throws IOException {

        BufferedReader reader = new BufferedReader(new StringReader(rawEmail));
        String line = reader.readLine();
        Map<String, String> headers = new HashMap<>();
        StringBuilder body = new StringBuilder();
        boolean headersDone = false;
        boolean htmlStarted = false;
        boolean finalSpace = false;
        String boundary = null;

        while (line != null) {

            if (line.isEmpty() && !headersDone) {

                headersDone = true;
                line = reader.readLine();
                continue;

            }

            if (headersDone) {

                if (htmlStarted) {

                    if (finalSpace) {

                        if (boundary != null && !line.contains("--" + boundary)) {

                            if (!line.isEmpty()) {
                                body.append(line).append("\n");
                                line = reader.readLine();
                                continue;
                            }

                            break;

                        }

                    } else {

                        if  (line.isEmpty()) {finalSpace = true; }
                        else {
                            line = reader.readLine();
                            continue;
                        }

                    }

                } else {

                    int i = line.indexOf(':');

                    if (i != -1) {

                        if (line.substring(0, i).equalsIgnoreCase("Content-Type") && line.substring(i + 1).contains("html")) {

                            htmlStarted = true;

                        }

                    }
                }

            } else {

                int i = line.indexOf(':');

                if (i != -1) {

                    String headerName = line.substring(0, i);
                    String headerValue = line.substring(i + 1);
                    headers.put(headerName, headerValue);

                    if (headerName.equalsIgnoreCase("Content-Type") && headerValue.contains("boundary")) {
                        boundary = headerValue.split("boundary=")[1].replaceAll("\"", "");
                    }

                }

            }

            line = reader.readLine();
        }

        return body.toString();

    }

    private AdminEmailObject parseEmail(String rawEmail) throws MessagingException, IOException {
        Session session = Session.getInstance(new Properties());
        InputStream is = new ByteArrayInputStream(rawEmail.getBytes());
        MimeMessage message = new MimeMessage(session, is);
        AdminEmailObject emailObject = new AdminEmailObject();
        BufferedReader br = new BufferedReader(new StringReader(rawEmail));

        emailObject.setId(br.readLine());
        br.close();
        emailObject.setDate(message.getSentDate());
        emailObject.setSubject(message.getSubject());
        emailObject.setTo(message.getRecipients(Message.RecipientType.TO)[0].toString());
        emailObject.setFrom(message.getFrom()[0].toString());

        emailObject.setBody(getTextFromMessage(message));

        List<EmailAttachment> attachments = new ArrayList<>();
        if (message.getContent() instanceof Multipart) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                        bodyPart.getFileName() != null) {
                    EmailAttachment attachment = new EmailAttachment();
                    attachment.setFileName(bodyPart.getFileName());
                    attachment.setContentType(bodyPart.getContentType());
                    attachment.setContentBase64(
                            Base64.getEncoder().encodeToString(bodyPart.getInputStream().readAllBytes())
                    );
                    attachments.add(attachment);
                }
            }
        }
        emailObject.setAttachments(attachments);

        return emailObject;
    }


    private String getTextFromMessage(MimeMessage message) throws MessagingException, IOException {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        } else if (content instanceof Multipart) {
            return getTextFromMultipart((Multipart) content);
        }
        return "";
    }

    private String getTextFromMultipart(Multipart multipart) throws MessagingException, IOException {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if (bodyPart.isMimeType("text/plain")) {
                return (String) bodyPart.getContent();
            } else if (bodyPart.isMimeType("text/html")) {
                return (String) bodyPart.getContent();
            } else if (bodyPart.getContent() instanceof Multipart) {
                return getTextFromMultipart((Multipart) bodyPart.getContent());
            }
        }
        return "";
    }


    private AdminEmailObject parseRawEmailToObject(String rawEmail) throws IOException, ParseException {

        BufferedReader reader = new BufferedReader(new StringReader(rawEmail));
        String line = reader.readLine();
        Map<String, String> headers = new HashMap<>();

        StringBuilder body = new StringBuilder();
        boolean headersDone = false;
        boolean contentStarted = false;
        String boundary = null;

        if (line != null) {
            headers.put("id", line);
        }

        while (line != null) {

            if (line.isEmpty() && !headersDone) {

                headersDone = true;
                line = reader.readLine();
                continue;

            }

            if (headersDone) {

                if (!headers.get("Content-Type").contains("multipart")) {

                    body.append(line).append("\n");
                    line = reader.readLine();
                    continue;

                } else if (contentStarted) {

                    if (boundary != null && !line.equals("--" + boundary)) {

                        body.append(line).append("\n");
                        line = reader.readLine();
                        continue;

                    }

                    break;

                } else {

                    if (line.isEmpty()) {
                        contentStarted = true;
                        line = reader.readLine();
                        continue;

                    }

                }

            } else {

                int i = line.indexOf(':');

                if (i != -1) {

                    String headerName = line.substring(0, i);
                    String headerValue = line.substring(i + 1);
                    headers.put(headerName, headerValue);

                    if (headerName.equalsIgnoreCase("Content-Type")) {
                        if (headerValue.contains("boundary")) {
                            boundary = headerValue.split("boundary=")[1].replaceAll("\"", "");
                        } else {
                            line = reader.readLine();
                            if (line.contains("boundary")) {
                                boundary = line.split("boundary=")[1].replaceAll("\"", "");
                            }
                        }

                    }

                }

            }

            line = reader.readLine();
        }

        AdminEmailObject aeo = new AdminEmailObject();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        aeo.setId(headers.get("id"));
        aeo.setTo(headers.get("To"));
        aeo.setFrom(headers.get("From"));
        aeo.setSubject(headers.get("Subject"));
        aeo.setDate(formatter.parse(headers.get("Date").trim()));
        aeo.setBody(body.toString());

        return aeo;

    }

}