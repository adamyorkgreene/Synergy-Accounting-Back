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
        userDTO.getRole().ifPresentOrElse(user::setUserType, () -> userDTO.setIsIncomplete(true));

        if (userDTO.getIsIncomplete()) throw new UserAttributeMissingException("Missing essential user data attributes.");

        user.setBirthday(userDTO.getBirthday().isPresent() ? userDTO.getBirthday().get(): 0);
        user.setBirthMonth(userDTO.getBirthMonth().isPresent() ? userDTO.getBirthMonth().get(): 0);
        user.setBirthYear(userDTO.getBirthYear().isPresent() ? userDTO.getBirthYear().get(): 0);
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
        User createdUser = userRepository.save(user);

        userService.sendResetPasswordEmail(user.getEmail());

        return ResponseEntity.ok(createdUser);
    }

    public ResponseEntity<?> updateUser(Long id, UserDTO userDetails) {

        User existingUser = userRepository.findByUserid(id);

        existingUser.setEmail(userDetails.getEmail().isEmpty() ? existingUser.getEmail() : userDetails.getEmail().get());
        existingUser.setUsername(userDetails.getUsername().isEmpty() ? existingUser.getUsername() : userDetails.getUsername().get());
        existingUser.setFirstName(userDetails.getFirstName().isEmpty() ? existingUser.getLastName() : userDetails.getFirstName().get());
        existingUser.setLastName(userDetails.getLastName().isEmpty() ? existingUser.getLastName() : userDetails.getLastName().get());
        existingUser.setBirthday(userDetails.getBirthday().isEmpty() ? existingUser.getBirthday() : userDetails.getBirthday().get());
        existingUser.setBirthMonth(userDetails.getBirthMonth().isEmpty() ? existingUser.getBirthMonth() : userDetails.getBirthMonth().get());
        existingUser.setBirthYear(userDetails.getBirthYear().isEmpty() ? existingUser.getBirthYear() : userDetails.getBirthYear().get());
        existingUser.setIsVerified(userDetails.getIsVerified().isEmpty() ? existingUser.isVerified() : userDetails.getIsVerified().get());
        existingUser.setIsActive(userDetails.getIsActive().isEmpty() ? existingUser.isActive() : userDetails.getIsActive().get());
        existingUser.setUserType(userDetails.getRole().isEmpty() ? existingUser.getUserType() : userDetails.getRole().get());

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
