package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.EmailObject;
import edu.kennesaw.appdomain.dto.NewPasswordRequest;
import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.dto.RegistrationRequest;
import edu.kennesaw.appdomain.dto.VerificationRequest;
import edu.kennesaw.appdomain.repository.TokenRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.service.EmailService;
import edu.kennesaw.appdomain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@CrossOrigin(origins = "https://synergyaccounting.app")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(registrationRequest.getUsername());
        user.setPassword(registrationRequest.getPassword());
        user.setIsVerified(false);
        return userService.registerUser(user, registrationRequest.getConfpassword());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        return userService.loginUser(user.getUsername(), user.getPassword());
    }

    @PostMapping("/verify")
    public ResponseEntity<User> verifyUser(@RequestBody VerificationRequest verificationRequest) {
        User verifiedUser = userService.verifyUser(verificationRequest.getUserId(), verificationRequest.getVerificationCode());
        if (verifiedUser != null) {
            return ResponseEntity.ok(verifiedUser);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestResetPassword(@RequestBody EmailObject email) {
        User user = userService.getUserFromEmail(email.getEmail());
        if (user != null) {
            String token = UUID.randomUUID().toString();
            System.out.println(token);
            userService.savePasswordResetToken(user, token);
            String resetLink = "https://synergyaccounting.app/password-reset?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            return ResponseEntity.ok("Password reset link has been sent.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/password-reset")
    public ResponseEntity<String> showPasswordResetForm(@RequestParam("token") String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok("Token is Valid.");

    }

    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, @RequestBody NewPasswordRequest password) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = resetToken.getUser();
        user.setPassword(password.getPassword());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        return ResponseEntity.ok("Password Reset Successfully");
    }


}
