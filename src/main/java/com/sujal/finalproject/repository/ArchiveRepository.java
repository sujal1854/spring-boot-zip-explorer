package com.sujal.finalproject.repository;

import com.sujal.finalproject.model.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchiveRepository extends JpaRepository<Archive, Long> {

    // Supports the GET /api/v1/archives?name=... endpoint
    Page<Archive> findByDisplayNameContainingIgnoreCaseOrOriginalFileNameContainingIgnoreCase(
            String displayName, String originalFileName, Pageable pageable);
}