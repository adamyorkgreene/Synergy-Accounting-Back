package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.User;
import edu.kennesaw.appdomain.dto.ErrorResponse;
import edu.kennesaw.appdomain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> registerUser(User user, String confpassword) {
        String email = user.getEmail();
        String username = user.getUsername();
        String password = user.getPassword();
        if (userRepository.findByEmail(email) == null) {
            if (userRepository.findByUsername(username) == null) {
                if (password.equals(confpassword)) {
                    if (email.contains("@") && email.contains(".")) {
                        if (password.length() > 5) {
                            return ResponseEntity.ok(userRepository.save(user));
                        }
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Password must be at least 6 characters."));
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Email address is invalid."));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Passwords do not match."));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("This username already exists."));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("An account already exists using this email."));
    }

    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

}
