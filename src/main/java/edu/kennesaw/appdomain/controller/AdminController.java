package edu.kennesaw.appdomain.controller;


import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.UserDTO;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.exception.UserAttributeMissingException;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.service.AdminService;
import edu.kennesaw.appdomain.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EmailService emailService;

    @Autowired
    UserRepository userRepository;

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) throws UserAttributeMissingException, IOException, InterruptedException {
        return adminService.createUserWithRole(user);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/updateuser")
    public ResponseEntity<?> updateuser(@RequestBody UserDTO user) throws IOException, InterruptedException {
        if (user.getUserid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UserID must be valid.");
        }
        return adminService.updateUser(user);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam boolean status){
        return adminService.setActiveStatus(id, status);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/usersearch")
    public ResponseEntity<?> findUser(@RequestBody UserDTO user) {
        int userid = 0;
        String email = "";
        String username = "";
        if (user.getUserid().isPresent()) {
            userid = user.getUserid().get();
        }
        if (user.getEmail().isPresent()) {
            email = user.getEmail().get();
        }
        if (user.getUsername().isPresent()) {
            username = user.getUsername().get();
        }
        if (userRepository.findById(Integer.toUnsignedLong(userid)).isPresent()) {
            return ResponseEntity.ok(userRepository.findById(Integer.toUnsignedLong(userid)).get());
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.ok(userRepository.findByEmail(email).get());
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.ok(userRepository.findByUsername(username));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/send-email")
    public ResponseEntity<?> sendAdminEmail(@RequestBody AdminEmailObject aem) {
        emailService.sendAdminEmail(aem.getTo(), aem.getFrom(), aem.getSubject(), aem.getBody());
        return ResponseEntity.ok().body(new MessageResponse("Email sent.")) ;
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/emails/{username}")
    public ResponseEntity<?> getMail(@PathVariable("username") String username) {
        List<AdminEmailObject> emails = emailService.getUserEmails(username);
        if (emails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new MessageResponse("No emails found."));
        }
        return ResponseEntity.ok(emails);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/emails/delete")
    public ResponseEntity<?> deleteMail(@RequestBody AdminEmailObject[] emails) {
        if (emails.length == 0) return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Selection is Empty!");
        if (emailService.deleteEmails(emails)) return ResponseEntity.ok(new MessageResponse("Email deleted successfully!"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Email could not be deleted!"));
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/get-all-emails/accountant")
    public ResponseEntity<?> getAllAccountantEmails() {
        return adminService.getAllAccountantEmails();
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/get-all-emails/manager")
    public ResponseEntity<?> getAllManagerEmails() {
        return adminService.getAllManagerEmails();
    }

}
