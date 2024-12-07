package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.dto.NewUserDTO;
import edu.kennesaw.appdomain.entity.UserDate;
import edu.kennesaw.appdomain.entity.UserSecurity;
import edu.kennesaw.appdomain.repository.*;
import edu.kennesaw.appdomain.types.UserType;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.UserDTO;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.exception.UserAttributeMissingException;
import edu.kennesaw.appdomain.service.utils.ServiceUtils;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private EntityManager em;

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
        userSecurity.setPassword(uuid, passwordEncoder);
        userSecurity.setIsActive(true);
        userSecurity.setIsVerified(true);
        userSecurity.setFailedLoginAttempts(0);
        userSecurity.setUser(user);

        user.setUserDate(userDate);
        user.setUserSecurity(userSecurity);

        User createdUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        userService.savePasswordResetToken(createdUser, token);
        String resetLink = "https://synergyaccounting.app/password-reset?token=" + token;
        emailService.sendPasswordSetEmail(user.getEmail(), resetLink);

        if (!createdUser.getUserType().equals(UserType.DEFAULT)) {
            scriptService.createMailbox(createdUser);
        }
        return ResponseEntity.ok(createdUser);
    }

    @Transactional
    public ResponseEntity<?> updateUser(NewUserDTO userDetails) throws IOException, InterruptedException {

        Optional<User> optionalExistingUser = userRepository.findByUserid(userDetails.getUserid());

        if (optionalExistingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found."));
        }

        User existingUser = optionalExistingUser.get();

        String oldUsername = existingUser.getUsername();

        UserType existingUserType = existingUser.getUserType();
        UserType newUserType = userDetails.getUserType();

        if (existingUserType.equals(UserType.DEFAULT)) {
            if (!newUserType.equals(UserType.DEFAULT)) {
                scriptService.createMailbox(existingUser);
            }
        } else {
            if (newUserType.equals(UserType.DEFAULT)) {
                if (scriptService.mailboxExists(existingUser)) scriptService.deleteMailbox(existingUser);
            }
        }

        existingUser.setEmail(userDetails.getEmail());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setUserType(userDetails.getUserType());

        UserSecurity userSecurity = userSecurityRepository.findById(existingUser.getUserSecurity().getId()).orElse(new UserSecurity());
        userSecurity.setIsVerified(userDetails.getUserSecurity().getIsVerified());
        userSecurity.setIsActive(userDetails.getUserSecurity().getIsActive());
        userSecurity.setFailedLoginAttempts(userDetails.getUserSecurity().getFailedLoginAttempts());

        existingUser.setUserSecurity(userSecurityRepository.save(userSecurity));

        UserDate userDate = userDateRepository.findById(existingUser.getUserDate().getId()).orElse(new UserDate());
        userDate.setTempLeaveStart(userDetails.getUserDate().getTempLeaveStart());
        userDate.setTempLeaveEnd(userDetails.getUserDate().getTempLeaveEnd());
        userDate.setBirthday(userDetails.getUserDate().getBirthday());

        existingUser.setUserDate(userDateRepository.save(userDate));

        if (!oldUsername.equals(existingUser.getUsername())) {
            scriptService.updateUsername(oldUsername.toLowerCase(), existingUser.getUsername().toLowerCase());
        }

        Path oldFilePath = Paths.get("/home/sweappdomain/demobackend/uploads/").resolve(oldUsername + ".jpg");
        Path newFilePath = Paths.get("/home/sweappdomain/demobackend/uploads/").resolve(existingUser.getUsername() + ".jpg");

        if (Files.exists(oldFilePath)) {
            Files.move(oldFilePath, newFilePath);
        }

        return ResponseEntity.ok(userRepository.save(existingUser));

    }

    public ResponseEntity<?> deleteUserRequest(Long userid) {
        if (userRepository.existsById(userid)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Transactional
    public ResponseEntity<?> deleteUserConfirm(Long userid) throws IOException, InterruptedException {
        Optional<User> userOptional = userRepository.findById(userid);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            User defaultDeletedUser = userRepository.findByUsername("deleted_user")
                    .orElseGet(this::createDefaultDeletedUser);

            updateAndDeleteUserReferences(user, defaultDeletedUser);

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

    public List<NewUserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<NewUserDTO> usersDTO = new ArrayList<>();
        users.forEach(user -> usersDTO.add(new NewUserDTO(user)));
        return usersDTO;
    }

    public List<NewUserDTO> getExpiredPasswords() {
        List<UserSecurity> userSecurities = userSecurityRepository.getAllByIsPasswordExpired(true);
        List<User> users = new ArrayList<>();
        userSecurities.forEach(userSecurity -> users.add(userSecurity.getUser()));
        List<NewUserDTO> usersDTO = new ArrayList<>();
        users.forEach(user -> usersDTO.add(new NewUserDTO(user)));
        return usersDTO;
    }

    private User createDefaultDeletedUser() {
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
        userSecurity.setPassword(uuid, passwordEncoder);
        userSecurity.setIsActive(false);
        userSecurity.setIsVerified(true);
        userSecurity.setFailedLoginAttempts(0);
        userSecurity.setUser(defaultDeletedUserNew);

        defaultDeletedUserNew.setUserSecurity(userSecurity);
        defaultDeletedUserNew.setUserDate(userDate);

        User deletedUser = userRepository.save(defaultDeletedUserNew);

        try {
            scriptService.createMailbox(deletedUser);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return userRepository.save(deletedUser);
    }

    private void updateAndDeleteUserReferences(User user, User defaultDeletedUser) {

        accountRepository.updateCreatorByCreator(user, defaultDeletedUser);
        em.flush();
        eventLogRepository.updateUserIdByUserId(user.getUserid(), defaultDeletedUser.getUserid());
        em.flush();
        journalEntryRepository.updateCreatorByCreator(user, defaultDeletedUser);
        em.flush();

        oldPasswordRepository.deleteAllByUserSecurity(user.getUserSecurity());
        em.flush();
        userSecurityRepository.delete(user.getUserSecurity());
        em.flush();
        userDateRepository.delete(user.getUserDate());
        em.flush();

        verificationRepository.deleteAllByUser(user);
        em.flush();
        tokenRepository.deleteAllByUser(user);
        em.flush();
        confirmationRepository.deleteAllByUser(user);
        em.flush();
    }

}
