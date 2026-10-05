package com.sujal.finalproject.model.dto;

import jakarta.validation.constraints.Size;

public class ArchiveUpdateRequest {

    @Size(max = 255, message = "Display name must be less than 255 characters")
    private String displayName;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}