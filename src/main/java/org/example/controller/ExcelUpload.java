package org.example.controller;

import org.example.service.ExcelUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
public class ExcelUpload {

    @Autowired
    private ExcelUploadService uploadService;

    @PostMapping("/{type}")
    public ResponseEntity<String> uploadExcel(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file) {
        try {
            uploadService.uploadExcel(file, type);
            return ResponseEntity.ok("Successfully uploaded " + type + " data");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Upload failed: " + e.getMessage());
        }
    }
}