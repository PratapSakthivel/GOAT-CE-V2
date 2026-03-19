package com.goatce.controller;

import com.goatce.dto.FileResponse;
import com.goatce.service.FileSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicFileController {

    private final FileSystemService fileSystemService;

    public PublicFileController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<FileResponse> getPublicFile(@PathVariable String token) {
        return ResponseEntity.ok(fileSystemService.getPublicFile(token));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
