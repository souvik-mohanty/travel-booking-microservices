package com.tourflow.file.service;

import com.tourflow.file.domain.FileMetadata;
import com.tourflow.file.dto.FileMetadataResponse;
import com.tourflow.file.exception.FileAccessDeniedException;
import com.tourflow.file.exception.FileNotFoundException;
import com.tourflow.file.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;

    public FileService(
            FileMetadataRepository fileMetadataRepository,
            FileStorageService fileStorageService
    ) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public FileMetadataResponse uploadFile(MultipartFile file, UUID ownerId) {

        String storageKey = fileStorageService.store(file);

        FileMetadata metadata = new FileMetadata(
                UUID.randomUUID(),
                ownerId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                storageKey,
                OffsetDateTime.now()
        );

        FileMetadata saved = fileMetadataRepository.save(metadata);

        return FileMetadataResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public FileMetadataResponse getFile(UUID id) {

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        return FileMetadataResponse.fromEntity(metadata);
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getMyFiles(UUID ownerId) {

        return fileMetadataRepository.findByOwnerId(ownerId)
                .stream()
                .map(FileMetadataResponse::fromEntity)
                .toList();
    }

    // Returns the raw bytes plus the metadata needed for response headers.
    // Owner-only for now -- there's no authorization model yet for sharing a
    // document with e.g. an ops reviewer, so the safe default is the same
    // "only the uploader can read it back" rule used everywhere else in this
    // project when there's no one else to grant access to.
    @Transactional(readOnly = true)
    public DownloadableFile downloadFile(UUID id, UUID callerId) {

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        if (!metadata.getOwnerId().equals(callerId)) {
            throw new FileAccessDeniedException("You cannot download another user's file");
        }

        byte[] content = fileStorageService.read(metadata.getStorageKey());

        return new DownloadableFile(metadata.getFileName(), metadata.getContentType(), content);
    }

    @Transactional
    public void deleteFile(UUID id, UUID callerId) {

        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        if (!metadata.getOwnerId().equals(callerId)) {
            throw new FileAccessDeniedException("You cannot delete another user's file");
        }

        fileStorageService.delete(metadata.getStorageKey());
        fileMetadataRepository.delete(metadata);
    }

    public record DownloadableFile(
            String fileName,
            String contentType,
            byte[] content
    ) {
    }
}
