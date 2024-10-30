package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.UserDate;
import edu.kennesaw.appdomain.entity.UserSecurity;
import edu.kennesaw.appdomain.repository.*;
import edu.kennesaw.appdomain.types.UserType;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.UserDTO;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.exception.UserAttributeMissingException;
import edu.kennesaw.appdomain.service.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Autowired
    private OldPasswordRepository oldPasswordRepository;

    @Autowired
    private UserDateRepository userDateRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ConfirmationRepository confirmationRepository;

    @Autowired
    private EventLogRepository eventLogRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private EmailService emailService;

    public ResponseEntity<?> createUserWithRole(UserDTO userDTO) throws UserAttributeMissingException, IOException, InterruptedException {

        userDTO.setIsIncomplete(false);

        User user = new User();

        userDTO.getEmail().ifPresentOrElse(user::setEmail, () -> userDTO.setIsIncomplete(true));
        userDTO.getFirstName().ifPresentOrElse(user::setFirstName, () -> userDTO.setIsIncomplete(true));
        userDTO.getLastName().ifPresentOrElse(user::setLastName, () -> userDTO.setIsIncomplete(true));
        userDTO.getUserType().ifPresentOrElse(user::setUserType, () -> userDTO.setIsIncomplete(true));
        user.setAddress(userDTO.getAddress().isPresent() ? userDTO.getAddress().get() : null);
        user.setUsername(ServiceUtils.generateUsername(user.getFirstName(), user.getLastName(), userRepository));

        if (userDTO.getIsIncomplete()) throw new UserAttributeMissingException("Missing essential user data attributes.");

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("This user already exists."));
        }

        UserDate userDate = new UserDate();
        userDate.setBirthday(userDTO.getBirthday().isPresent() ? userDTO.getBirthday().get(): null);
        userDate.setJoinDate(new Date());
        userDate.setUser(user);

        UserSecurity userSecurity = new UserSecurity();
        String uuid = UUID.randomUUID().toString();
        userSecurity.setPassword(passwordEncoder.encode(uuid));
        userSecurity.setIsActive(true);
        userSecurity.setIsVerified(true);
        userSecurity.setFailedLoginAttempts(0);
        userSecurity.setUser(user);

        user.setUserDate(userDate);
        user.setUserSecurity(userSecurity);

        User createdUser = userRepository.save(user);

        userService.sendResetPasswordEmail(user.getEmail());

        if (!createdUser.getUserType().equals(UserType.DEFAULT) && !createdUser.getUserType().equals(UserType.USER)) {
            scriptService.createMailbox(createdUser);
        }

        return ResponseEntity.ok(createdUser);
    }

    public ResponseEntity<?> updateUser(UserDTO userDetails) throws IOException, InterruptedException {

        if (userDetails.getUserid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error passing userid."));
        }

        Optional<User> optionalExistingUser = userRepository.findByUserid(userDetails.getUserid().get());

        if (optionalExistingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found."));
        }

        User existingUser = optionalExistingUser.get();

        String oldUsername = existingUser.getUsername();

        if (userDetails.getUserType().isPresent()) {
            UserType existingUserType = existingUser.getUserType();
            UserType newUserType = userDetails.getUserType().get();
            if (existingUserType.equals(UserType.DEFAULT) || existingUserType.equals(UserType.USER)) {
                if (!newUserType.equals(UserType.DEFAULT) && !newUserType.equals(UserType.USER)) {
                    scriptService.createMailbox(existingUser);
                }
            } else {
                if (newUserType.equals(UserType.DEFAULT) || newUserType.equals(UserType.USER)) {
                    scriptService.deleteMailbox(existingUser);
                }
            }
        }

        existingUser.setEmail(userDetails.getEmail().isEmpty() ? existingUser.getEmail() : userDetails.getEmail().get());
        existingUser.setUsername(userDetails.getUsername().isEmpty() ? existingUser.getUsername() : userDetails.getUsername().get());
        existingUser.setFirstName(userDetails.getFirstName().isEmpty() ? existingUser.getLastName() : userDetails.getFirstName().get());
        existingUser.setLastName(userDetails.getLastName().isEmpty() ? existingUser.getLastName() : userDetails.getLastName().get());
        existingUser.getUserDate().setBirthday(userDetails.getBirthday().isEmpty() ? existingUser.getUserDate().getBirthday() : userDetails.getBirthday().get());
        existingUser.getUserSecurity().setIsVerified(userDetails.getIsVerified().isEmpty() ? existingUser.getUserSecurity().getIsVerified() : userDetails.getIsVerified().get());
        existingUser.getUserSecurity().setIsActive(userDetails.getIsActive().isEmpty() ? existingUser.getUserSecurity().getIsActive() : userDetails.getIsActive().get());
        existingUser.setUserType(userDetails.getUserType().isEmpty() ? existingUser.getUserType() : userDetails.getUserType().get());
        existingUser.getUserSecurity().setFailedLoginAttempts(userDetails.getFailedPasswordAttempts().isEmpty() ? existingUser.getUserSecurity().getFailedLoginAttempts() : userDetails.getFailedPasswordAttempts().get());
        existingUser.getUserDate().setTempLeaveStart(userDetails.getTempLeaveStart().isEmpty() ? existingUser.getUserDate().getTempLeaveStart() : userDetails.getTempLeaveStart().get());
        existingUser.getUserDate().setTempLeaveEnd(userDetails.getTempLeaveEnd().isEmpty() ? existingUser.getUserDate().getTempLeaveEnd() : userDetails.getTempLeaveEnd().get());
        existingUser.getUserSecurity().setEmailPassword(userDetails.getEmailPassword().isEmpty() ? existingUser.getUserSecurity().getEmailPassword() : userDetails.getEmailPassword().get());
        userRepository.save(existingUser);

        if (!oldUsername.equals(existingUser.getUsername())) {
            scriptService.updateUsername(oldUsername.toLowerCase(), existingUser.getUsername().toLowerCase());
        }

        Path oldFilePath = Paths.get("/home/sweappdomain/demobackend/uploads/").resolve(oldUsername + ".jpg");
        Path newFilePath = Paths.get("/home/sweappdomain/demobackend/uploads/").resolve(existingUser.getUsername() + ".jpg");

        if (Files.exists(oldFilePath)) {
            Files.move(oldFilePath, newFilePath);
        }

        return ResponseEntity.ok(existingUser);

    }

    public ResponseEntity<?> deleteUserRequest(Long userid) {
        if (userRepository.existsById(userid)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> deleteUserConfirm(Long userid) throws IOException, InterruptedException {

        if (userRepository.findById(userid).isPresent()) {

            User defaultDeletedUser;
            Optional<User> defaultDeletedUserOptional = userRepository.findByUsername("deleted_user");
            if (defaultDeletedUserOptional.isEmpty()) {
                User defaultDeletedUserNew = new User();
                defaultDeletedUserNew.setUsername("deleted_user");
                defaultDeletedUserNew.setEmail("deleted_user@synergyaccounting.app");
                defaultDeletedUserNew.setFirstName("deleted");
                defaultDeletedUserNew.setLastName("user");
                defaultDeletedUserNew.setUserType(UserType.ADMINISTRATOR);

                UserDate userDate = new UserDate();
                userDate.setBirthday(new Date());
                userDate.setJoinDate(new Date());
                userDate.setUser(defaultDeletedUserNew);

                UserSecurity userSecurity = new UserSecurity();
                String uuid = UUID.randomUUID().toString();
                userSecurity.setPassword(passwordEncoder.encode(uuid));
                userSecurity.setIsActive(false);
                userSecurity.setIsVerified(true);
                userSecurity.setFailedLoginAttempts(0);
                userSecurity.setUser(defaultDeletedUserNew);

                defaultDeletedUserNew.setUserSecurity(userSecurity);
                defaultDeletedUserNew.setUserDate(userDate);

                defaultDeletedUser = userRepository.save(defaultDeletedUserNew);

            } else {
                defaultDeletedUser = defaultDeletedUserOptional.get();
            }

            User user = userRepository.findById(userid).get();
            UserSecurity userSecurity = user.getUserSecurity();

            oldPasswordRepository.deleteAllByUserSecurity(userSecurity);
            userSecurityRepository.delete(user);
            userDateRepository.deleteByUser(user);

            verificationRepository.deleteByUser(user);
            tokenRepository.deleteByUser(user);
            confirmationRepository.deleteByUser(user);

            accountRepository.updateCreatorByCreator(user, defaultDeletedUser);
            eventLogRepository.updateUserIdByUserId(user.getUserid(), defaultDeletedUser.getUserid());
            journalEntryRepository.updateCreatorByCreator(user, defaultDeletedUser);

            File file = new File("/home/sweappdomain/demobackend/uploads/" + user.getUserid() + ".jpg");
            if (scriptService.mailboxExists(user)) scriptService.deleteMailbox(user);
            if (file.exists()) file.delete();

            userRepository.delete(user);

            return ResponseEntity.ok(new MessageResponse("Successfully deleted user: " + user.getUsername()));

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<?> setActiveStatus(Long id, boolean status) {
        User user = userRepository.findByUserid(id).isPresent() ? userRepository.findByUserid(id).get() :
                null;
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        user.getUserSecurity().setIsActive(status);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User: "+ (status ? "activated" : "deactivated") + "successfully"));
    }

}
