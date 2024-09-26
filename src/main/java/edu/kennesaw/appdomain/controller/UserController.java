package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.UserType;
import edu.kennesaw.appdomain.dto.*;
import edu.kennesaw.appdomain.entity.ConfirmationToken;
import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.ConfirmationRepository;
import edu.kennesaw.appdomain.repository.TokenRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.service.EmailService;
import edu.kennesaw.appdomain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Autowired
    private ConfirmationRepository confirmationRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest registrationRequest) {
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setBirthday(registrationRequest.getBirthday());
        user.setBirthMonth(registrationRequest.getBirthMonth());
        user.setBirthYear(registrationRequest.getBirthYear());
        user.setAddress(registrationRequest.getAddress());
        user.setPassword(registrationRequest.getPassword());
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat year = new SimpleDateFormat("yy");
        SimpleDateFormat month = new SimpleDateFormat("MM");
        String username = registrationRequest.getFirstName().charAt(0) + registrationRequest.getLastName() + month.format(gc.getTime()) + year.format(gc.getTime());
        if (userRepository.findByUsername(username) != null) {
            int increment = 1;
            while (userRepository.findByUsername(username) != null) {
                username += "-" + increment;
                increment++;
            }
        }
        user.setUsername(username);
        return userService.registerUser(user, registrationRequest.getConfpassword());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        return userService.loginUser(user.getEmail(), user.getPassword());
    }

    @PostMapping("/verify")
    public ResponseEntity<User> verifyUser(@RequestBody VerificationRequest verificationRequest) {
        User verifiedUser = userService.verifyUser(verificationRequest.getUserId(), verificationRequest.getVerificationCode());
        if (verifiedUser != null) {
            System.out.println("Userid: " + verifiedUser.getUserid());
            return ResponseEntity.ok(verifiedUser);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestResetPassword(@RequestBody EmailObject email) {
        User user = userService.getUserFromEmail(email.getEmail());
        if (user != null) {
            String token = UUID.randomUUID().toString();
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

    @PostMapping("/request-confirm-user")
    public ResponseEntity<String> requestConfirmUser(@RequestBody ConfirmationRequest cr) {
        User user = userRepository.findByUserid(cr.getUserid());
        System.out.println(cr.getUserid());
        if (user != null) {
            System.out.println(user.getFirstName());
            String token = UUID.randomUUID().toString();
            System.out.println(token);
            userService.saveConfirmationToken(user, token);
            String confirmLink = "https://synergyaccounting.app/confirm-user?token=" + token;
            List<User> admins = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getUserType().equals(UserType.ADMINISTRATOR))
                    .collect(Collectors.toList());
            for (int i = 0; i < admins.size(); i += 1) {
                int end = Math.min(admins.size(), i + 1);
                List<User> batch = admins.subList(i, end);
                for (User admin : batch) {
                    emailService.sendAdminConfirmEmail(admin.getEmail(), user, confirmLink);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return ResponseEntity.ok("Confirmation email has been sent.");
        }
        System.out.println("USER IS NULL");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/confirm-user")
    public ResponseEntity<String> confirmUser(@RequestParam("token") String token) {
        ConfirmationToken confToken = confirmationRepository.findByToken(token);
        if (confToken == null || confToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = confToken.getUser();
        user.setUserType(UserType.USER);
        userRepository.save(user);
        confirmationRepository.delete(confToken);
        return ResponseEntity.ok("Token is Valid.");
    }

}
