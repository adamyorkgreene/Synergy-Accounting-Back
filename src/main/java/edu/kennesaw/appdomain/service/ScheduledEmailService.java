package edu.kennesaw.appdomain.service;

import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.UserRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduledEmailService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendPasswordExpirationNotifications() {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime expiryDate = currentDate.plusDays(3);

        List<User> usersWithExpiringPasswords = userRepository.findUsersWithExpiringPasswords(3);
        
        for (User user : usersWithExpiringPasswords) {
            emailService.sendPasswordExpirationNotification(user.getEmail(), user.getUsername());
        }
    }
}
