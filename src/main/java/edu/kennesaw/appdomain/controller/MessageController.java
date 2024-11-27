package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.service.GeneralMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/dashboard")
public class MessageController {

    @Autowired
    private GeneralMessageService generalMessageService;

    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MANAGER', 'ACCOUNTANT')")
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages() {
        return ResponseEntity.ok(generalMessageService.getGeneralMessages());
    }

}
