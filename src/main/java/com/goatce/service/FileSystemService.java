package com.goatce.service;

import com.goatce.dto.*;
import com.goatce.CodeFile;
import com.goatce.CollabRoom;
import com.goatce.FileVersion;
import com.goatce.Folder;
import com.goatce.User;
import com.goatce.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileSystemService {

    private final FolderRepository folderRepository;
    private final CodeFileRepository codeFileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final UserRepository userRepository;

    public FileSystemService(FolderRepository folderRepository,
                              CodeFileRepository codeFileRepository,
                              FileVersionRepository fileVersionRepository,
                              UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.codeFileRepository = codeFileRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.userRepository = userRepository;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private FileResponse toFileResponse(CodeFile file) {
        return new FileResponse(
                file.getId(),
                file.getName(),
                file.getLanguage(),
                file.getContent(),
                file.getFolder().getId(),
                file.getFolder().getName(),
                file.isPublic(),
                file.getPublicToken(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }

    // ─── Folders ────────────────────────────────────────────────────────────

    public FolderResponse createFolder(String email, FolderRequest request) {
        User user = getCurrentUser(email);

        if (folderRepository.existsByNameAndOwnerEmail(request.name(), email)) {
            throw new RuntimeException("Folder name already exists");
        }

        Folder folder = Folder.builder()
                .name(request.name())
                .owner(user)
                .build();

        folder = folderRepository.save(folder);
        return new FolderResponse(folder.getId(), folder.getName(), folder.getCreatedAt());
    }

    public List<FolderResponse> getFolders(String email) {
        return folderRepository.findByOwnerEmail(email).stream()
                .map(f -> new FolderResponse(f.getId(), f.getName(), f.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void deleteFolder(String email, UUID folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        folderRepository.delete(folder);
    }

    // ─── Files ──────────────────────────────────────────────────────────────

    public FileResponse createFile(String email, UUID folderId, FileRequest request) {
        User user = getCurrentUser(email);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        CodeFile file = CodeFile.builder()
                .name(request.name())
                .language(request.language())
                .content(request.content() != null ? request.content() : "")
                .folder(folder)
                .owner(user)
                .isPublic(false)
                .build();

        file = codeFileRepository.save(file);
        return toFileResponse(file);
    }

    public List<FileResponse> getFilesInFolder(String email, UUID folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        return codeFileRepository.findByFolderId(folderId).stream()
                .map(this::toFileResponse)
                .collect(Collectors.toList());
    }

    public FileResponse getFile(String email, UUID fileId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        return toFileResponse(file);
    }

    public FileResponse saveContent(String email, UUID fileId, SaveContentRequest request) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        file.setContent(request.content());

        Integer currentCount = fileVersionRepository.countByCodeFileId(fileId);
        int nextVersion = (currentCount == null ? 0 : currentCount) + 1;

        FileVersion version = FileVersion.builder()
                .codeFile(file)
                .content(request.content())
                .versionNumber(nextVersion)
                .build();

        fileVersionRepository.save(version);
        file = codeFileRepository.save(file);
        return toFileResponse(file);
    }

    public List<FileVersionResponse> getVersionHistory(String email, UUID fileId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        return fileVersionRepository.findByCodeFileIdOrderByVersionNumberDesc(fileId).stream()
                .map(v -> new FileVersionResponse(v.getId(), v.getVersionNumber(), v.getContent(), v.getSavedAt()))
                .collect(Collectors.toList());
    }

    public FileResponse restoreVersion(String email, UUID fileId, UUID versionId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        FileVersion version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        file.setContent(version.getContent());

        Integer currentCount = fileVersionRepository.countByCodeFileId(fileId);
        int nextVersion = (currentCount == null ? 0 : currentCount) + 1;

        FileVersion newVersion = FileVersion.builder()
                .codeFile(file)
                .content(version.getContent())
                .versionNumber(nextVersion)
                .build();

        fileVersionRepository.save(newVersion);
        file = codeFileRepository.save(file);
        return toFileResponse(file);
    }

    public FileResponse makePublic(String email, UUID fileId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        file.setPublic(true);
        file.setPublicToken(token);
        file = codeFileRepository.save(file);
        return toFileResponse(file);
    }

    public FileResponse makePrivate(String email, UUID fileId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        file.setPublic(false);
        file.setPublicToken(null);
        file = codeFileRepository.save(file);
        return toFileResponse(file);
    }

    public FileResponse getPublicFile(String token) {
        CodeFile file = codeFileRepository.findByPublicToken(token)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return toFileResponse(file);
    }

    public List<FileResponse> searchFiles(String email, String query) {
        return codeFileRepository.findByOwnerEmailAndNameContainingIgnoreCase(email, query).stream()
                .map(this::toFileResponse)
                .collect(Collectors.toList());
    }

    public void deleteFile(String email, UUID fileId) {
        CodeFile file = codeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!file.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        codeFileRepository.delete(file);
    }
}
