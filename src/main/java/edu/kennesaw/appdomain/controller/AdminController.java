package edu.kennesaw.appdomain.controller;


import edu.kennesaw.appdomain.dto.AdminEmailObject;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.UserDTO;
import edu.kennesaw.appdomain.dto.UserUpdate;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.exception.UserAttributeMissingException;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.service.AdminService;
import edu.kennesaw.appdomain.service.EmailService;
import edu.kennesaw.appdomain.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) throws UserAttributeMissingException {
        return adminService.createUserWithRole(user);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/updateuser")
    public ResponseEntity<?> updateuser(@RequestBody UserDTO user){
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
        Optional<User> foundUser = Optional.empty();
        if (user.getUserid().isPresent()) {
            foundUser = Optional.ofNullable(userRepository.findByUserid(user.getUserid().get()));
        }
        if (foundUser.isEmpty() && user.getEmail().isPresent()) {
            foundUser = Optional.ofNullable(userRepository.findByEmail(user.getEmail().get()));
        }
        if (foundUser.isEmpty() && user.getUsername().isPresent()) {
            foundUser = Optional.ofNullable(userRepository.findByUsername(user.getUsername().get()));
        }
        if (foundUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("That user does not exist."));
        }
        return ResponseEntity.ok(foundUser.get());
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/send-email")
    public ResponseEntity<?> sendAdminEmail(@RequestBody AdminEmailObject aem) throws MessagingException {
        emailService.sendAdminEmail(aem.getTo(), aem.getSubject(), aem.getBody());
        return ResponseEntity.ok().body(new MessageResponse("Email sent.")) ;
    }

}
