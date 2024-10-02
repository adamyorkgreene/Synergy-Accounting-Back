package edu.kennesaw.appdomain.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
public class CsrfController {

    @GetMapping("/api/csrf")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("token", csrfToken.getToken());

            System.out.println("Session ID: " + session.getId());
            System.out.println("CSRF Token: " + csrfToken.getToken());

            return ResponseEntity.ok()
                    .header("X-CSRF-TOKEN", csrfToken.getToken())  // Ensure the token is included in the header
                    .body(tokenMap);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "CSRF token not found"));
        }
    }
}
