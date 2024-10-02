package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.UserType;
import edu.kennesaw.appdomain.dto.LoginRequest;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.RegistrationRequest;
import edu.kennesaw.appdomain.entity.ConfirmationToken;
import edu.kennesaw.appdomain.entity.PasswordResetToken;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.entity.VerificationToken;
import edu.kennesaw.appdomain.repository.ConfirmationRepository;
import edu.kennesaw.appdomain.repository.TokenRepository;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.repository.VerificationRepository;
import edu.kennesaw.appdomain.service.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public ResponseEntity<?> registerUser(RegistrationRequest registrationRequest) {
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setBirthday(registrationRequest.getBirthday());
        user.setAddress(registrationRequest.getAddress());
        user.setPassword(registrationRequest.getPassword());
        user.setJoinDate(registrationRequest.getJoinDate());
        user.setUsername(ServiceUtils.generateUsername(registrationRequest.getFirstName(), registrationRequest.getLastName(), userRepository));
        String email = user.getEmail();
        String password = user.getPassword();
        if (userRepository.findByEmail(email) == null) {
            if (password.equals(registrationRequest.getConfpassword())) {
                if (email.contains("@") && email.contains(".") && !email.contains(" ") && email.length() > 6) {
                    if (password.length() >= 8 && !password.contains(" ")) {
                        user.setPassword(passwordEncoder.encode(password));
                        userRepository.save(user);
                        String token = UUID.randomUUID().toString();
                        saveVerificationToken(user, token);
                        String confirmLink = "https://synergyaccounting.app/verify?token=" + token;
                        emailService.sendVerificationEmail(user.getEmail(), confirmLink);
                        return ResponseEntity.ok(
                                new MessageResponse("A verification link has been sent to your email.")
                        );
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            new MessageResponse("Password must be at least 8 characters and not contain spaces.")
                    );
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new MessageResponse("Email address is invalid.")
                );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new MessageResponse("Passwords do not match.")
            );
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new MessageResponse("An account already exists using this email.")
        );
    }

    public ResponseEntity<?> loginUser(LoginRequest lr) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(lr.getEmail(), lr.getPassword())
            );

            User authenticatedUser = userRepository.findByEmail(lr.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Authenticated User: " + SecurityContextHolder.getContext().getAuthentication().getName());

            if (authenticatedUser.getFailedLoginAttempts() >= 3) {
                return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse("Your account has been locked." +
                        " Please reset your password."));
            }

            authenticatedUser.setFailedLoginAttempts(0);
            userRepository.save(authenticatedUser);
            return ResponseEntity.ok(authenticatedUser);

        } catch (BadCredentialsException e) {

            User invalidUser = userRepository.findByEmail(lr.getEmail());

            if (invalidUser != null) {
                invalidUser.setFailedLoginAttempts(invalidUser.getFailedLoginAttempts() + 1);
                userRepository.save(invalidUser);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid username or password!")
                );

            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("This email does not exist in our database!")
            );

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An error occurred during login")
            );

        }
    }

    public ResponseEntity<MessageResponse> resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        tokenRepository.delete(resetToken);
        return ResponseEntity.ok(new MessageResponse("Password reset was successful!"));
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

    public ResponseEntity<MessageResponse> sendResetPasswordEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            savePasswordResetToken(user, token);
            String resetLink = "https://synergyaccounting.app/password-reset?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            return ResponseEntity.ok(new MessageResponse("A link to reset your password has been sent" +
                    " to your email.")
            );
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Invalid email address."));
    }

}
