package com.sujal.finalproject.model.dto;

import com.sujal.finalproject.model.entity.Archive;
import com.sujal.finalproject.model.entity.ArchiveEntry;

public class ArchiveMapper {

    public static ArchiveDto toDto(Archive archive) {
        ArchiveDto dto = new ArchiveDto();
        dto.setId(archive.getId());
        dto.setOriginalFileName(archive.getOriginalFileName());
        dto.setDisplayName(archive.getDisplayName());
        dto.setDescription(archive.getDescription());
        dto.setSizeBytes(archive.getSizeBytes());
        dto.setEntryCount(archive.getEntryCount());
        dto.setUploadedAt(archive.getUploadedAt());
        return dto;
    }

    public static ArchiveNode toNode(ArchiveEntry entry) {
        return new ArchiveNode(
                entry.getName(),
                entry.getPath(),
                entry.getType(),
                entry.getSizeBytes()
        );
    }
}