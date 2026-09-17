package com.tourflow.file.controller;

import com.tourflow.file.dto.FileMetadataResponse;
import com.tourflow.file.service.FileService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMetadataResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        FileMetadataResponse response = fileService.uploadFile(file, ownerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<FileMetadataResponse>> getMyFiles(
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                fileService.getMyFiles(ownerId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadataResponse> getFile(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                fileService.getFile(id)
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        FileService.DownloadableFile file = fileService.downloadFile(id, callerId);

        MediaType mediaType = file.contentType() != null
                ? MediaType.parseMediaType(file.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.fileName() != null ? file.fileName() : "download")
                                .build()
                                .toString()
                )
                .body(file.content());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        fileService.deleteFile(id, callerId);

        return ResponseEntity.noContent().build();
    }
}
