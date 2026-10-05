package com.sujal.finalproject.service;

import com.sujal.finalproject.config.ArchiveProperties;
import com.sujal.finalproject.exception.LimitExceededException;
import com.sujal.finalproject.exception.ResourceNotFoundException;
import com.sujal.finalproject.model.dto.ArchiveNode;
import com.sujal.finalproject.model.entity.Archive;
import com.sujal.finalproject.model.entity.ArchiveEntry;
import com.sujal.finalproject.repository.ArchiveEntryRepository;
import com.sujal.finalproject.repository.ArchiveRepository;
import com.sujal.finalproject.storage.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final ArchiveEntryRepository archiveEntryRepository;
    private final FileStorageService fileStorageService;
    private final ZipProcessingService zipProcessingService;
    private final ArchiveProperties properties;

    public ArchiveService(ArchiveRepository archiveRepository,
                          ArchiveEntryRepository archiveEntryRepository,
                          FileStorageService fileStorageService,
                          ZipProcessingService zipProcessingService,
                          ArchiveProperties properties) {
        this.archiveRepository = archiveRepository;
        this.archiveEntryRepository = archiveEntryRepository;
        this.fileStorageService = fileStorageService;
        this.zipProcessingService = zipProcessingService;
        this.properties = properties;
    }

    @Transactional
    public Long uploadArchive(MultipartFile file) {
        if (file.getSize() > properties.getMaxUploadSizeBytes()) {
            throw new LimitExceededException("File size exceeds upload limit.");
        }

        String storedFileName = fileStorageService.store(file);                                                  //store and return storedname UUID wtv
        File storedFile = fileStorageService.getFile(storedFileName);                                            //path to storedfile location

        try {
            zipProcessingService.validateZipSignature(storedFile);
            List<ArchiveTreeBuilder.FlatEntry> flatEntries = zipProcessingService.extractFlatEntries(storedFile);

            if (flatEntries.size() > properties.getMaxEntryCount()) {
                throw new LimitExceededException("Archive contains too many entries.");
            }

            long totalUncompressedSize = flatEntries.stream().mapToLong(ArchiveTreeBuilder.FlatEntry::size).sum();
            if (totalUncompressedSize > properties.getMaxUncompressedSizeBytes()) {
                throw new LimitExceededException("Uncompressed size exceeds allowed limit.");
            }

            Archive archive = new Archive();
            archive.setOriginalFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.zip");
            archive.setStoredFileName(storedFileName);
            archive.setSizeBytes(file.getSize());
            archive.setEntryCount(flatEntries.size());
            archive.setUploadedAt(LocalDateTime.now());

            List<ArchiveEntry> entries = flatEntries.stream().map(flat -> {
                ArchiveEntry entry = new ArchiveEntry();
                entry.setArchive(archive);
                entry.setPath(flat.path());

                String rawPath = flat.path().replace("\\", "/");
                boolean isFolder = rawPath.endsWith("/");
                String normalizedPath = isFolder ? rawPath.substring(0, rawPath.length() - 1) : rawPath;

                entry.setName(normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1));
                entry.setParentPath(normalizedPath.contains("/") ? normalizedPath.substring(0, normalizedPath.lastIndexOf('/')) : "");
                entry.setType(isFolder ? "FOLDER" : "FILE");
                entry.setSizeBytes(flat.size());
                return entry;
            }).collect(Collectors.toList());

            archive.setEntries(entries);
            Archive savedArchive = archiveRepository.save(archive);
            return savedArchive.getId();

        } catch (Exception e) {
            fileStorageService.delete(storedFileName);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<Archive> listArchives(String name, Pageable pageable) {
        if (name != null && !name.trim().isEmpty()) {
            return archiveRepository.findByDisplayNameContainingIgnoreCaseOrOriginalFileNameContainingIgnoreCase(name, name, pageable);
        }
        return archiveRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Archive getArchive(Long id) {
        return archiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public ArchiveNode getArchiveTree(Long id) {
        Archive archive = getArchive(id);
        List<ArchiveEntry> dbEntries = archiveEntryRepository.findByArchiveId(id);

        List<ArchiveTreeBuilder.FlatEntry> flatEntries = dbEntries.stream()
                .map(e -> new ArchiveTreeBuilder.FlatEntry(e.getPath(), e.getSizeBytes() != null ? e.getSizeBytes() : 0))
                .collect(Collectors.toList());

        return ArchiveTreeBuilder.buildTree(archive.getOriginalFileName(), flatEntries);
    }

    @Transactional(readOnly = true)
    public List<ArchiveEntry> getFolderChildren(Long id, String path) {
        if (!archiveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Archive not found with ID: " + id);
        }

        // Security check as requested in manual
        if (path.contains("..") || path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path requested.");
        }

        // Normalize trailing slashes for query
        String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        return archiveEntryRepository.findByArchiveIdAndParentPath(id, normalizedPath);
    }

    @Transactional
    public void updateArchive(Long id, String displayName, String description) {
        Archive archive = getArchive(id);
        archive.setDisplayName(displayName);
        archive.setDescription(description);
        archiveRepository.save(archive);
    }

    @Transactional
    public void deleteArchive(Long id) {
        Archive archive = getArchive(id);
        fileStorageService.delete(archive.getStoredFileName());
        archiveRepository.delete(archive);
    }

    @Transactional(readOnly = true)
    public org.springframework.core.io.Resource loadFileAsResource(Long id) {
        Archive archive = getArchive(id);
        File file = fileStorageService.getFile(archive.getStoredFileName());

        if (!file.exists() || !file.canRead()) {
            throw new ResourceNotFoundException("File not found on disk for archive ID: " + id);
        }

        try {
            return new org.springframework.core.io.UrlResource(file.toURI());
        } catch (Exception e) {
            throw new RuntimeException("Could not read the file for archive ID: " + id, e);
        }
    }
}