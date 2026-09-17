package com.tourflow.platform.file.repository;

import com.tourflow.platform.file.domain.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByOwnerId(UUID ownerId);
}
