package com.sujal.finalproject.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "archive_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"archive_id", "path"}) //This guarantees we never accidentally store the exact same file path twice for the same ZIP file.
})
public class ArchiveEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String parentPath;

    @Column(nullable = false)
    private String type; // "FILE" or "FOLDER"

    private Long sizeBytes;

    private LocalDateTime lastModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentPath() { return parentPath; }
    public void setParentPath(String parentPath) { this.parentPath = parentPath; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public Archive getArchive() { return archive; }
    public void setArchive(Archive archive) { this.archive = archive; }
}