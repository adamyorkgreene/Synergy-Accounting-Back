package edu.kennesaw.appdomain.controller;


import edu.kennesaw.appdomain.dto.UserDTO;
import edu.kennesaw.appdomain.dto.UserUpdate;
import edu.kennesaw.appdomain.exception.UserAttributeMissingException;
import edu.kennesaw.appdomain.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) throws UserAttributeMissingException {
        return adminService.createUserWithRole(user);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/updateuser/{id}")
    public ResponseEntity<?> updateuser(@RequestBody UserUpdate userUpdate){
        return adminService.updateUser(userUpdate.getUserid(), userUpdate.getUser());
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam boolean status){
        return adminService.setActiveStatus(id, status);
    }


}
