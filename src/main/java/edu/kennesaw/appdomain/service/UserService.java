package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.entity.ConfirmationToken;
import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.VerificationToken;
import edu.kennesaw.appdomain.repository.ConfirmationRepository;
import edu.kennesaw.appdomain.repository.TokenRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired EmailService emailService;

    public ResponseEntity<?> registerUser(User user, String confpassword) {
        String email = user.getEmail();
        String password = user.getPassword();
        if (userRepository.findByEmail(email) == null) {
            if (password.equals(confpassword)) {
                if (email.contains("@") && email.contains(".") && !email.contains(" ") && email.length() > 6) {
                    if (password.length() >= 8 && !password.contains(" ")) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                        String token = UUID.randomUUID().toString();
                        saveVerificationToken(user, token);
                        String confirmLink = "https://synergyaccounting.app/verify?token=" + token;
                        emailService.sendVerificationEmail(user.getEmail(), confirmLink);
                        return ResponseEntity.ok(new MessageResponse("A verification link has been sent to your email."));
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            new MessageResponse("Password must be at least 8 characters and not contain spaces."));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new MessageResponse("Email address is invalid."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new MessageResponse("Passwords do not match."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new MessageResponse("An account already exists using this email."));
    }

    public ResponseEntity<?> loginUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User authenticatedUser = userRepository.findByEmail(email);

            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("User does not exist!"));
            }
            /*if (!authenticatedUser.getPassword().equals(passwordEncoder.encode(password))) {
                authenticatedUser.setFailedLoginAttempts(authenticatedUser.getFailedLoginAttempts() + 1);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Incorrect Password!"));
            }*/
            if (authenticatedUser.getFailedLoginAttempts() >= 3) {
                return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse("Your account has been locked." +
                        " Please reset your password."));
            }
            authenticatedUser.setFailedLoginAttempts(0);
            userRepository.save(authenticatedUser);
            return ResponseEntity.ok(authenticatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid Credentials."));
        }
    }

    public ResponseEntity<MessageResponse> resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = resetToken.getUser();

// Check if the new password has been used previously
        if (isPasswordInHistory(user, newPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("New password cannot be the same as any previously used password."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        tokenRepository.delete(resetToken);

// Save the new password in history
        saveNewPasswordToHistory(user, newPassword);

        return ResponseEntity.ok(new MessageResponse("Password reset was successful!"));
    }

// Check if the password was used before
    private boolean isPasswordInHistory(User user, String newPassword) {
        List<String> oldPasswords = userRepository.findAllPasswordsByUserId(user.getID());

        for (String oldPassword : oldPasswords) {
            if (passwordEncoder.matches(newPassword, oldPassword)) {
                return true; // The new password is in the history
            }
        }
        return false; // The new password can be used
    }

// Save user password in history after pass. reset
    private void saveNewPasswordToHistory(User user, String newPassword) {
        userRepository.saveNewPassword(user.getId(), passwordEncoder.encode(newPassword));
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
