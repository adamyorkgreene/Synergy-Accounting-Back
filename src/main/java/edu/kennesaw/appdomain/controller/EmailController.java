package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.dto.EmailAttachment;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.ReadResponseDTO;
import edu.kennesaw.appdomain.service.EmailService;
import edu.kennesaw.appdomain.service.MailboxReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private MailboxReaderService mailboxReaderService;

    @PostMapping(value = "/send-email", consumes = "multipart/form-data")
    public ResponseEntity<?> sendAdminEmail(
            @RequestParam("to") String to,
            @RequestParam("from") String from,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<EmailAttachment> emailAttachments = null;

        if (attachments != null && !attachments.isEmpty()) {
            emailAttachments = attachments.stream().map(file -> {
                try {
                    EmailAttachment attachment = new EmailAttachment();
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setContentBase64(Base64.getEncoder().encodeToString(file.getBytes()));
                    attachment.setContentType(file.getContentType());
                    return attachment;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process attachment: " + file.getOriginalFilename(), e);
                }
            }).toList();
        }

        emailService.sendAdminEmail(to, from, subject, body, emailAttachments);
        return ResponseEntity.ok().body(new MessageResponse("Email sent."));
    }

    @GetMapping("/emails/{username}")
    public ResponseEntity<?> getMail(@PathVariable("username") String username) {
        List<AdminEmailObject> emails = emailService.getUserEmails(username);
        if (emails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new MessageResponse("No emails found."));
        }
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/emails/unread/{username}")
    public ResponseEntity<?> getUnreadEmailCount(@PathVariable("username") String username) {
        try {
            int unreadCount = mailboxReaderService.getUnreadEmailCount(username);
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching unread emails.");
        }
    }


    @PostMapping("/mark-as-read")
    public ResponseEntity<?> markAsRead(@RequestBody ReadResponseDTO dto) {
        boolean result = mailboxReaderService.markAsRead(dto);
        if (result) return ResponseEntity.ok(result);
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/emails/delete")
    public ResponseEntity<?> deleteMail(@RequestBody AdminEmailObject[] emails) {
        if (emails.length == 0) return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Selection is Empty!");
        if (emailService.deleteEmails(emails)) return ResponseEntity.ok(new MessageResponse("Email deleted successfully!"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Email could not be deleted!"));
    }

    @GetMapping("/get-all-emails/accountant")
    public ResponseEntity<?> getAllAccountantEmails() {
        return emailService.getAllAccountantEmails();
    }

    @GetMapping("/get-all-emails/manager")
    public ResponseEntity<?> getAllManagerEmails() {
        return emailService.getAllManagerEmails();
    }

}
