package edu.kennesaw.appdomain.controller;

import edu.kennesaw.appdomain.dto.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CrossOrigin(origins = "https://synergyaccounting.app", allowCredentials = "true")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("No file selected", HttpStatus.BAD_REQUEST);
        }
        String name = request.getHeader("User");
        try {
            String uploadDir = "/home/sweappdomain/demobackend/uploads/";
            File uploadPath = new File(uploadDir + name + ".jpg");
            if (!uploadPath.exists()) {
                boolean created = uploadPath.mkdirs();
                if (!created) {
                    return new ResponseEntity<>("Could not create upload directory", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            file.transferTo(uploadPath);
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to upload file: " + e.getMessage());
            return new ResponseEntity<>("Could not upload the file. Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>("Unexpected error occurred. Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<?> serveFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("/home/sweappdomain/demobackend/uploads/").resolve(filename);
        if (Files.exists(filePath)) {
            Resource resource = new FileSystemResource(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath)).body(resource);
        } else {
            Path defaultFilePath = Paths.get("/home/sweappdomain/demobackend/uploads/default.jpg");
            Resource resource = new FileSystemResource(defaultFilePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(defaultFilePath)).body(resource);
        }
    }
}
