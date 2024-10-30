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
    UserRepository userRepository;

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) throws UserAttributeMissingException, IOException, InterruptedException {
        return adminService.createUserWithRole(user);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/updateuser")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO user) throws IOException, InterruptedException {
        if (user.getUserid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UserID must be valid.");
        }
        return adminService.updateUser(user);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/delete-user-request")
    public ResponseEntity<?> deleteUserRequest(@RequestBody Long userId) {
        return adminService.deleteUserRequest(userId);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/delete-user-confirm")
    public ResponseEntity<?> deleteUser(@RequestBody Long userId) throws IOException, InterruptedException {
        return adminService.deleteUserConfirm(userId);
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

}
