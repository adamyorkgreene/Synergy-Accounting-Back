package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.UserDate;
import edu.kennesaw.appdomain.entity.UserSecurity;
import edu.kennesaw.appdomain.types.UserType;
import edu.kennesaw.appdomain.dto.MessageResponse;
import edu.kennesaw.appdomain.dto.UserDTO;
import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.exception.UserAttributeMissingException;
import edu.kennesaw.appdomain.repository.UserRepository;
import edu.kennesaw.appdomain.service.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    public UserRepository userRepository;

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

        if (createdUser.getUserType().equals(UserType.ADMINISTRATOR)) {
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
            if (!existingUser.getUserType().equals(UserType.ADMINISTRATOR)) {
                if (userDetails.getUserType().get().equals(UserType.ADMINISTRATOR)) {
                    scriptService.createMailbox(existingUser);
                }
            } else {
                if (!userDetails.getUserType().get().equals(UserType.ADMINISTRATOR)) {
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
