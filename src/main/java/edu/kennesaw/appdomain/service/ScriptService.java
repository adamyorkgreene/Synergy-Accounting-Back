package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class ScriptService {

    private static final Logger log = LoggerFactory.getLogger(ScriptService.class);


    public void createMailbox(User user) throws IOException, InterruptedException {
        String username = user.getUsername().toLowerCase();
        String emailPassword = user.getUserSecurity().getEmailPassword();
        String scriptPath = "/home/sweappdomain/demobackend/scripts/add_vmailbox_user.sh";
        log.info("Running script with arguments: sudo {}, {}, {}", scriptPath, username, emailPassword);
        if (username.isEmpty() || emailPassword == null || emailPassword.isEmpty()) {
            throw new IllegalArgumentException("Username or email password is missing");
        }
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", scriptPath, username, emailPassword);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append(System.lineSeparator());
            }
        }
        int exitCode = process.waitFor();
        log.info("Script Output for creating mailbox: \n{}", output);
        log.error("Script Error Output for creating mailbox: \n{}", errorOutput);
        log.info("Script Exit Code: {}", exitCode);
    }

    public void deleteMailbox(User user) throws IOException, InterruptedException {
        String scriptPath = "/home/sweappdomain/demobackend/scripts/delete_vmailbox_user.sh";
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", scriptPath, user.getUsername().toLowerCase());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to delete mailbox for user: " + user.getUsername());
        }
    }

    public void updateUsername(String oldUsername, String newUsername) throws IOException, InterruptedException {
        String scriptPath = "/home/sweappdomain/demobackend/scripts/update_vmailbox_user.sh";
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", scriptPath, oldUsername.toLowerCase(), newUsername.toLowerCase());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Failed to change mailbox username from: " + oldUsername + " to: " + newUsername);
        }
    }

    public boolean mailboxExists(User user) throws IOException, InterruptedException {
        String scriptPath = "/home/sweappdomain/demobackend/scripts/check_vmailbox_user.sh";
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", scriptPath, user.getUsername().toLowerCase());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        boolean mailboxExists = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Mailbox exists")) {
                    mailboxExists = true;
                }
            }
        }
        process.waitFor();
        return mailboxExists;
    }
}
