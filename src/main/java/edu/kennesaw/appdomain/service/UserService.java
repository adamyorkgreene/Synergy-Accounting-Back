package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.dto.ErrorResponse;
import edu.kennesaw.appdomain.repository.TokenRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailService ems;

    public ResponseEntity<?> registerUser(User user, String confpassword) {
        String email = user.getEmail();
        String username = user.getUsername();
        String password = user.getPassword();
        if (userRepository.findByEmail(email) == null) {
            if (userRepository.findByUsername(username) == null) {
                if (password.equals(confpassword)) {
                    if (email.contains("@") && email.contains(".") && !email.contains(" ")) {
                        if (!username.isBlank() && !username.contains(" ")) {
                            if (password.length() > 5 && !password.contains(" ")) {
                                User registeredUser = userRepository.save(user);
                                ems.sendNoReplyEmail(email, registeredUser.getVerificationCode());
                                return ResponseEntity.ok(registeredUser);
                            }
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                    new ErrorResponse("Password must be at least 6 characters and not contain spaces."));
                        }
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                new ErrorResponse("Username cannot be blank or contain spaces"));
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            new ErrorResponse("Email address is invalid."));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new ErrorResponse("Passwords do not match."));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse("This username already exists."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse("An account already exists using this email."));
    }

    public ResponseEntity<?> loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
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

    public User verifyUser(Long id, String verificationCode) {
        User verifiedUser = userRepository.findByUserid(id);
        if (verifiedUser != null) {
            if (verifiedUser.getVerificationCode().equals(verificationCode)) {
                verifiedUser.setIsVerified(true);
                userRepository.save(verifiedUser);
                return verifiedUser;
            }
        }
        return null;
    }

    public User getUserFromEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void savePasswordResetToken(User user, String token) {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(30);
        tokenRepository.save(resetToken);
    }

}
