package edu.kennesaw.appdomain.service.utils;

import edu.kennesaw.appdomain.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class ServiceUtils {

    public static String generateUsername(String firstName, String lastName, UserRepository userRepository) {
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat year = new SimpleDateFormat("yy");
        SimpleDateFormat month = new SimpleDateFormat("MM");
        String username = firstName.charAt(0) + lastName
                + month.format(gc.getTime()) + year.format(gc.getTime()
        );
        if (userRepository.findByUsername(username) != null) {
            int increment = 1;
            while (userRepository.findByUsername(username) != null) {
                username += "-" + increment;
                increment++;
            }
        }
        return username;
    }

}
