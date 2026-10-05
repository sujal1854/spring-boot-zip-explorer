package com.sujal.finalproject.controller;

import com.sujal.finalproject.model.dto.ArchiveDto;
import com.sujal.finalproject.model.dto.ArchiveMapper;
import com.sujal.finalproject.model.dto.ArchiveNode;
import com.sujal.finalproject.model.dto.ArchiveUpdateRequest;
import com.sujal.finalproject.model.entity.Archive;
import com.sujal.finalproject.service.ArchiveService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/archives")
public class ArchiveController {

    private final ArchiveService archiveService;

    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> uploadArchive(@RequestParam("file") MultipartFile file) {
        Long id = archiveService.uploadArchive(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping
    public ResponseEntity<Page<ArchiveDto>> listArchives(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        Page<ArchiveDto> result = archiveService.listArchives(name, pageable)
                .map(ArchiveMapper::toDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchiveDto> getArchiveDetails(@PathVariable Long id) {
        ArchiveDto dto = ArchiveMapper.toDto(archiveService.getArchive(id));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<ArchiveNode> getArchiveTree(@PathVariable Long id) {
        ArchiveNode tree = archiveService.getArchiveTree(id);
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<ArchiveNode>> getFolderChildren(
            @PathVariable Long id,
            @RequestParam String path) {
        List<ArchiveNode> children = archiveService.getFolderChildren(id, path)
                .stream()
                .map(ArchiveMapper::toNode)
                .collect(Collectors.toList());
        return ResponseEntity.ok(children);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateArchive(
            @PathVariable Long id,
            @Valid @RequestBody ArchiveUpdateRequest request) {
        archiveService.updateArchive(id, request.getDisplayName(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArchive(@PathVariable Long id) {
        archiveService.deleteArchive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadArchive(@PathVariable Long id) {
        // Fetch archive to retrieve the original filename
        Archive archive = archiveService.getArchive(id);
        org.springframework.core.io.Resource resource = archiveService.loadFileAsResource(id);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archive.getOriginalFileName() + "\"")
                .body(resource);
    }
}