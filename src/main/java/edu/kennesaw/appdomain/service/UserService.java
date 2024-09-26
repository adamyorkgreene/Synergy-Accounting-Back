package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.ConfirmationToken;
import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.dto.ErrorResponse;
import edu.kennesaw.appdomain.entity.VerificationToken;
import edu.kennesaw.appdomain.repository.ConfirmationRepository;
import edu.kennesaw.appdomain.repository.TokenRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ConfirmationRepository confirmationRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<?> registerUser(User user, String confpassword) {
        String email = user.getEmail();
        String password = user.getPassword();
        if (userRepository.findByEmail(email) == null) {
            if (password.equals(confpassword)) {
                if (email.contains("@") && email.contains(".") && !email.contains(" ") && email.length() > 6) {
                    if (password.length() > 5 && !password.contains(" ")) {
                        user.setPassword(passwordEncoder.encode(password));
                        User registeredUser = userRepository.save(user);
                        return ResponseEntity.ok(registeredUser);
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            new ErrorResponse("Password must be at least 6 characters and not contain spaces."));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new ErrorResponse("Email address is invalid."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse("Passwords do not match."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse("An account already exists using this email."));
    }

    public ResponseEntity<?> loginUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                if (user.getFailedLoginAttempts() < 3) {
                    user.setFailedLoginAttempts(0);
                    return ResponseEntity.ok(userRepository.save(user));
                }
                return ResponseEntity.status(HttpStatus.LOCKED).body("Your account has been locked. Please reset your password.");
            }
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not exist!");
    }

    public ResponseEntity<String> resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        return ResponseEntity.ok("Password Reset Successfully");
    }

    public User getUserFromEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveVerificationToken(User user, String token) {
        VerificationToken verifyToken = new VerificationToken();
        verifyToken.setUser(user);
        verifyToken.setToken(token);
        verifyToken.setExpiryDate(30);
        verificationRepository.save(verifyToken);
    }

    public void savePasswordResetToken(User user, String token) {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(30);
        tokenRepository.save(resetToken);
    }

    public void saveConfirmationToken(User user, String token) {
        ConfirmationToken confToken = new ConfirmationToken();
        confToken.setUser(user);
        confToken.setToken(token);
        confToken.setExpiryDate(30);
        confirmationRepository.save(confToken);
    }

}
