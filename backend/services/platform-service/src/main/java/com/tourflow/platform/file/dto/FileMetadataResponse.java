package com.tourflow.platform.file.dto;

import com.tourflow.platform.file.domain.FileMetadata;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileMetadataResponse(
        UUID id,
        UUID ownerId,
        String fileName,
        String contentType,
        Long sizeBytes,
        OffsetDateTime createdAt
) {

    public static FileMetadataResponse fromEntity(FileMetadata file) {
        return new FileMetadataResponse(
                file.getId(),
                file.getOwnerId(),
                file.getFileName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getCreatedAt()
        );
    }
}
