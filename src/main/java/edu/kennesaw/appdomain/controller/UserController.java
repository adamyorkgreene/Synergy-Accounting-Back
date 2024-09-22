package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.User;
import edu.kennesaw.appdomain.dto.RegistrationRequest;
import edu.kennesaw.appdomain.dto.VerificationRequest;
import edu.kennesaw.appdomain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://synergyaccounting.app")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        return userService.registerUser(user, registrationRequest.getConfpassword());
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User user) {
        User loggedInUser = userService.loginUser(user.getUsername(), user.getPassword());
        if (loggedInUser != null) {
            return ResponseEntity.ok(loggedInUser);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/verify")
    public ResponseEntity<User> verifyUser(@RequestBody VerificationRequest verificationRequest) {
        User verifiedUser = userService.verifyUser(verificationRequest.getUser(), verificationRequest.getVerificationCode());
        if (verifiedUser != null) {
            verifiedUser.setIsVerified(true);
            return ResponseEntity.ok(verifiedUser);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
