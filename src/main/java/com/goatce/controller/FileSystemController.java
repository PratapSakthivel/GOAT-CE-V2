package com.goatce.controller;

import com.goatce.dto.*;
import com.goatce.service.FileSystemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileSystemController {

    private final FileSystemService fileSystemService;

    public FileSystemController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    // ─── Folder Endpoints ───────────────────────────────────────────────────

    @PostMapping("/folders")
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody FolderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.createFolder(email, request));
    }

    @GetMapping("/folders")
    public ResponseEntity<List<FolderResponse>> getFolders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.getFolders(email));
    }

    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable UUID folderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        fileSystemService.deleteFolder(email, folderId);
        return ResponseEntity.noContent().build();
    }

    // ─── File Endpoints ──────────────────────────────────────────────────────

    @PostMapping("/folders/{folderId}/files")
    public ResponseEntity<FileResponse> createFile(@PathVariable UUID folderId,
                                                    @Valid @RequestBody FileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.createFile(email, folderId, request));
    }

    @GetMapping("/folders/{folderId}/files")
    public ResponseEntity<List<FileResponse>> getFilesInFolder(@PathVariable UUID folderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.getFilesInFolder(email, folderId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getFile(@PathVariable UUID fileId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.getFile(email, fileId));
    }

    @PutMapping("/{fileId}/content")
    public ResponseEntity<FileResponse> saveContent(@PathVariable UUID fileId,
                                                     @Valid @RequestBody SaveContentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.saveContent(email, fileId, request));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        fileSystemService.deleteFile(email, fileId);
        return ResponseEntity.noContent().build();
    }

    // ─── Version Endpoints ──────────────────────────────────────────────────

    @GetMapping("/{fileId}/versions")
    public ResponseEntity<List<FileVersionResponse>> getVersionHistory(@PathVariable UUID fileId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.getVersionHistory(email, fileId));
    }

    @PostMapping("/{fileId}/versions/{versionId}/restore")
    public ResponseEntity<FileResponse> restoreVersion(@PathVariable UUID fileId,
                                                        @PathVariable UUID versionId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.restoreVersion(email, fileId, versionId));
    }

    // ─── Public/Private Endpoints ────────────────────────────────────────────

    @PostMapping("/{fileId}/make-public")
    public ResponseEntity<FileResponse> makePublic(@PathVariable UUID fileId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.makePublic(email, fileId));
    }

    @PostMapping("/{fileId}/make-private")
    public ResponseEntity<FileResponse> makePrivate(@PathVariable UUID fileId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.makePrivate(email, fileId));
    }

    // ─── Search Endpoint ────────────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<List<FileResponse>> searchFiles(@RequestParam String query) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(fileSystemService.searchFiles(email, query));
    }

    // ─── Exception Handler ──────────────────────────────────────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
