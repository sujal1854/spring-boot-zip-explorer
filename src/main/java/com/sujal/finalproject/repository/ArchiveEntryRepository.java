package com.sujal.finalproject.repository;

import com.sujal.finalproject.model.entity.ArchiveEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchiveEntryRepository extends JpaRepository<ArchiveEntry, Long> {

    // Derived Query : Get immediate children of a specific folder without scanning the whole DB
    List<ArchiveEntry> findByArchiveIdAndParentPath(Long archiveId, String parentPath);

    // Derived Query : Fetch all entries for an archive (useful to pass to the TreeBuilder)
    List<ArchiveEntry> findByArchiveId(Long archiveId);
}