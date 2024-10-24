package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<?> sendAdminEmail(@RequestBody AdminEmailObject aem) {
        emailService.sendAdminEmail(aem.getTo(), aem.getFrom(), aem.getSubject(), aem.getBody());
        return ResponseEntity.ok().body(new MessageResponse("Email sent.")) ;
    }

    @GetMapping("/emails/{username}")
    public ResponseEntity<?> getMail(@PathVariable("username") String username) {
        List<AdminEmailObject> emails = emailService.getUserEmails(username);
        if (emails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new MessageResponse("No emails found."));
        }
        return ResponseEntity.ok(emails);
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
