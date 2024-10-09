package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.AdminEmailObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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

    public List<AdminEmailObject> parseRawEmailsToObject(List<String> rawEmails) throws IOException, ParseException {

        List<AdminEmailObject> objectEmails = new ArrayList<>();

        for (String rawEmail : rawEmails) {
            objectEmails.add(parseRawEmailToObject(rawEmail));
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