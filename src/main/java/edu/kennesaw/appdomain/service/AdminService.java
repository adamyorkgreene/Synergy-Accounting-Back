package edu.kennesaw.appdomain.service;

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

import java.util.Date;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    public ResponseEntity<?> createUserWithRole(UserDTO userDTO) throws UserAttributeMissingException {

        userDTO.setIsIncomplete(false);

        final User user = new User();

        userDTO.getEmail().ifPresentOrElse(user::setEmail, () -> userDTO.setIsIncomplete(true));
        userDTO.getFirstName().ifPresentOrElse(user::setFirstName, () -> userDTO.setIsIncomplete(true));
        userDTO.getLastName().ifPresentOrElse(user::setLastName, () -> userDTO.setIsIncomplete(true));
        userDTO.getUserType().ifPresentOrElse(user::setUserType, () -> userDTO.setIsIncomplete(true));

        if (userDTO.getIsIncomplete()) throw new UserAttributeMissingException("Missing essential user data attributes.");

        user.setBirthday(userDTO.getBirthday().isPresent() ? userDTO.getBirthday().get(): null);
        //user.setBirthMonth(userDTO.getBirthMonth().isPresent() ? userDTO.getBirthMonth().get(): 0);
        //user.setBirthYear(userDTO.getBirthYear().isPresent() ? userDTO.getBirthYear().get(): 0);
        user.setAddress(userDTO.getAddress().isPresent() ? userDTO.getAddress().get() : null);

        String uuid = UUID.randomUUID().toString();

        user.setPassword(passwordEncoder.encode(uuid));
        user.setUsername(ServiceUtils.generateUsername(user.getFirstName(), user.getLastName(), userRepository));

        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("This user already exists."));
        }

        user.setIsActive(true);
        user.setIsVerified(true);
        user.setFailedLoginAttempts(0);

        user.setJoinDate(new Date());

        User createdUser = userRepository.save(user);

        userService.sendResetPasswordEmail(user.getEmail());

        return ResponseEntity.ok(createdUser);
    }

    public ResponseEntity<?> updateUser(UserDTO userDetails) {

        assert (userDetails.getUserid().isPresent());

        User existingUser = userRepository.findByUserid(userDetails.getUserid().get());

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User not found."));
        }

        existingUser.setEmail(userDetails.getEmail().isEmpty() ? existingUser.getEmail() : userDetails.getEmail().get());
        existingUser.setUsername(userDetails.getUsername().isEmpty() ? existingUser.getUsername() : userDetails.getUsername().get());
        existingUser.setFirstName(userDetails.getFirstName().isEmpty() ? existingUser.getLastName() : userDetails.getFirstName().get());
        existingUser.setLastName(userDetails.getLastName().isEmpty() ? existingUser.getLastName() : userDetails.getLastName().get());
        existingUser.setBirthday(userDetails.getBirthday().isEmpty() ? existingUser.getBirthday() : userDetails.getBirthday().get());
        existingUser.setIsVerified(userDetails.getIsVerified().isEmpty() ? existingUser.isVerified() : userDetails.getIsVerified().get());
        existingUser.setIsActive(userDetails.getIsActive().isEmpty() ? existingUser.isActive() : userDetails.getIsActive().get());
        existingUser.setUserType(userDetails.getUserType().isEmpty() ? existingUser.getUserType() : userDetails.getUserType().get());
        existingUser.setFailedLoginAttempts(userDetails.getFailedPasswordAttempts().isEmpty() ? existingUser.getFailedLoginAttempts() : userDetails.getFailedPasswordAttempts().get());
        existingUser.setTempLeaveStart(userDetails.getTempLeaveStart().isEmpty() ? existingUser.getTempLeaveStart() : userDetails.getTempLeaveStart().get());
        existingUser.setTempLeaveEnd(userDetails.getTempLeaveEnd().isEmpty() ? existingUser.getTempLeaveEnd() : userDetails.getTempLeaveEnd().get());

        userRepository.save(existingUser);

        return ResponseEntity.ok(existingUser);

    }

    public ResponseEntity<?> setActiveStatus(Long id, boolean status) {
        User user = userRepository.findByUserid(id);
        user.setIsActive(status);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User: "+ (status ? "activated" : "deactivated") + "successfully"));
    }

}
