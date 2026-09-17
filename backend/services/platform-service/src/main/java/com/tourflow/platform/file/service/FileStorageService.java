package com.tourflow.file.service;

import com.tourflow.file.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// Local-disk file storage -- a stand-in for S3, which isn't provisioned in
// this project. Same substitution pattern as local Postgres standing in for
// RDS: real I/O against real files, just not cloud-backed yet.
@Component
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(
            @Value("${file.storage-path}") String storagePath
    ) {
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(storageRoot);
        } catch (IOException ex) {
            throw new FileStorageException(
                    "Could not create file storage directory: " + storageRoot,
                    ex
            );
        }
    }

    // Store the upload under a random name (never the caller-supplied file
    // name) and return that storage key. Using a random name avoids both
    // path traversal and collisions between different uploads.
    public String store(MultipartFile file) {

        String storageKey = UUID.randomUUID().toString();
        Path target = storageRoot.resolve(storageKey);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store uploaded file", ex);
        }

        return storageKey;
    }

    public byte[] read(String storageKey) {

        Path source = resolve(storageKey);

        try {
            return Files.readAllBytes(source);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to read stored file", ex);
        }
    }

    public void delete(String storageKey) {

        Path target = resolve(storageKey);

        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to delete stored file", ex);
        }
    }

    // Resolve a storage key to a path strictly inside storageRoot -- storageKey
    // is always our own UUID, never caller input, but this guards against that
    // invariant ever being violated by a future caller.
    private Path resolve(String storageKey) {

        Path resolved = storageRoot.resolve(storageKey).normalize();

        if (!resolved.startsWith(storageRoot)) {
            throw new FileStorageException("Invalid storage key", null);
        }

        return resolved;
    }
}
