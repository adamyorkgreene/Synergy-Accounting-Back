package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.MessageResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public ResponseEntity<?> getCsrfToken(HttpServletRequest request) {

        HttpSession session = request.getSession(true);
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken == null) {
            return ResponseEntity.status(403).body(new MessageResponse("CSRF token not available."));
        } else {
            System.out.println("CSRF Token: " + csrfToken.getToken());
            System.out.println(session == null ? "No session found." : "Session ID: " + session.getId());
        }

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", csrfToken.getToken());

        return ResponseEntity.ok().header("X-CSRF-TOKEN", csrfToken.getToken()).body(tokenMap);

    }
}
