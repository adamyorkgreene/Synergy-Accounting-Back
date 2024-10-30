package edu.kennesaw.appdomain;

import edu.kennesaw.appdomain.entity.User;
import edu.kennesaw.appdomain.repository.UserRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

@SpringBootApplication
public class FinanceApplication {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().load();
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("MAILPW", dotenv.get("MAILPW"));

        SpringApplication.run(FinanceApplication.class, args);

    }

}
