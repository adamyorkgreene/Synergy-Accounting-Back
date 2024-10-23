package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.LoginRequest;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.RegistrationRequest;
import edu.kennesaw.appdomain.entity.*;
import edu.kennesaw.appdomain.repository.*;
import edu.kennesaw.appdomain.service.utils.ServiceUtils;
import edu.kennesaw.appdomain.types.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Autowired
    private EmailService emailService;

    public ResponseEntity<?> registerUser(RegistrationRequest registrationRequest) {

        if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("An account already exists using this email."));
        }

        String email = registrationRequest.getEmail();
        String password = registrationRequest.getPassword();

        if (!isValidEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Email address is invalid."));
        }

        if (!password.equals(registrationRequest.getConfpassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Passwords do not match."));
        }

        if (!isValidPassword(password)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Password must be at least 8 characters and not contain spaces."));
        }

        User user = new User();

        user.setEmail(email);
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setAddress(registrationRequest.getAddress());
        user.setUsername(ServiceUtils.generateUsername(registrationRequest.getFirstName(), registrationRequest.getLastName(), userRepository));
        user.setUserType(UserType.DEFAULT);

        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setPassword(passwordEncoder.encode(password));
        userSecurity.setIsActive(true);
        userSecurity.setIsVerified(false);
        userSecurity.setIsPasswordExpired(false);
        userSecurity.setOldPasswords(new HashSet<OldPassword>());
        userSecurity.setUser(user);

        UserDate userDate = new UserDate();
        userDate.setUser(user);
        userDate.setJoinDate(new Date());
        userDate.setLastPasswordReset(new Date());
        userDate.setTempLeaveEnd(null);
        userDate.setTempLeaveStart(null);
        userDate.setBirthday(registrationRequest.getBirthday());

        user.setUserSecurity(userSecurity);
        user.setUserDate(userDate);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        saveVerificationToken(user, token);
        String confirmLink = "https://synergyaccounting.app/verify?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), confirmLink);

        return ResponseEntity.ok(
                new MessageResponse("A verification link has been sent to your email.")
        );
    }

    public ResponseEntity<?> loginUser(LoginRequest lr) {

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(lr.getEmail(), lr.getPassword())
            );

            User authenticatedUser = userRepository.findByEmail(lr.getEmail()).isPresent() ?
                    userRepository.findByEmail(lr.getEmail()).get() : null;

            if (authenticatedUser == null) {
                throw new BadCredentialsException("That user does not exist.");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Authenticated User: " + SecurityContextHolder.getContext().getAuthentication().getName());

            if (authenticatedUser.getUserSecurity().getIsPasswordExpired()) {
                return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse("Your password has expired!" +
                        " Please reset it."));
            }

            Date lastPasswordReset = authenticatedUser.getUserDate().getLastPasswordReset();

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -90);

            if (lastPasswordReset.before(cal.getTime())) {
                authenticatedUser.getUserSecurity().setIsPasswordExpired(true);
                userRepository.save(authenticatedUser);
                return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse("Your password has expired!" +
                        " Please reset it."));
            }

            if (authenticatedUser.getUserSecurity().getFailedLoginAttempts() >= 3) {
                return ResponseEntity.status(HttpStatus.LOCKED).body(new MessageResponse("Your account has been locked." +
                        " Please reset your password."));
            }

            authenticatedUser.getUserSecurity().setFailedLoginAttempts(0);
            userRepository.save(authenticatedUser);

            return ResponseEntity.ok(authenticatedUser);

        } catch (BadCredentialsException e) {

            Optional<User> invalidUserOptional = userRepository.findByEmail(lr.getEmail());

            if (invalidUserOptional.isPresent()) {
                User invalidUser = invalidUserOptional.get();
                invalidUser.getUserSecurity().setFailedLoginAttempts(invalidUser
                        .getUserSecurity().getFailedLoginAttempts() + 1);
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

        if (!user.getUserSecurity().setPassword(passwordEncoder.encode(newPassword))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("You must change your password" +
                    " to one you have not previously used."));
        }

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
        PasswordResetToken prt = tokenRepository.findByUser(user);
        if (prt == null) {
            prt = new PasswordResetToken();
            prt.setUser(user);
        }
        prt.setToken(token);
        prt.setExpiryDate(30);
        tokenRepository.save(prt);
    }

    public void saveConfirmationToken(User user, String token) {
        ConfirmationToken confToken = new ConfirmationToken();
        confToken.setUser(user);
        confToken.setToken(token);
        confToken.setExpiryDate(30);
        confirmationRepository.save(confToken);
    }

    public ResponseEntity<MessageResponse> sendResetPasswordEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
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

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && !email.contains(" ") && email.length() > 6;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && !password.contains(" ");
    }

}
