package com.sujal.finalproject.service;

import com.sujal.finalproject.config.ArchiveProperties;
import com.sujal.finalproject.exception.LimitExceededException;
import com.sujal.finalproject.exception.ResourceNotFoundException;
import com.sujal.finalproject.model.entity.Archive;
import com.sujal.finalproject.repository.ArchiveEntryRepository;
import com.sujal.finalproject.repository.ArchiveRepository;
import com.sujal.finalproject.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveServiceTest {

    @Mock private ArchiveRepository archiveRepository;
    @Mock private ArchiveEntryRepository archiveEntryRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private ZipProcessingService zipProcessingService;
    @Mock private ArchiveProperties properties;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private ArchiveService archiveService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getMaxUploadSizeBytes()).thenReturn(1000L);
        lenient().when(properties.getMaxEntryCount()).thenReturn(5);
        lenient().when(properties.getMaxUncompressedSizeBytes()).thenReturn(5000L);
    }

    @Test
    void uploadArchive_Success() {
        when(multipartFile.getSize()).thenReturn(500L);
        when(multipartFile.getOriginalFilename()).thenReturn("test.zip");
        when(fileStorageService.store(multipartFile)).thenReturn("uuid.zip");
        when(fileStorageService.getFile("uuid.zip")).thenReturn(new File("uuid.zip"));

        List<ArchiveTreeBuilder.FlatEntry> mockEntries = List.of(new ArchiveTreeBuilder.FlatEntry("test.txt", 100));
        when(zipProcessingService.extractFlatEntries(any(File.class))).thenReturn(mockEntries);

        Archive mockArchive = new Archive();
        mockArchive.setId(1L);
        when(archiveRepository.save(any(Archive.class))).thenReturn(mockArchive);

        Long id = archiveService.uploadArchive(multipartFile);

        assertEquals(1L, id);
        verify(archiveRepository, times(1)).save(any(Archive.class));
    }

    @Test
    void uploadArchive_ThrowsException_WhenEntryCountExceeded() {
        when(multipartFile.getSize()).thenReturn(500L);
        when(fileStorageService.store(multipartFile)).thenReturn("uuid.zip");
        when(fileStorageService.getFile("uuid.zip")).thenReturn(new File("uuid.zip"));

        // Simulate a ZIP with 6 entries (limit is 5)
        List<ArchiveTreeBuilder.FlatEntry> mockEntries = List.of(
                new ArchiveTreeBuilder.FlatEntry("1.txt", 10), new ArchiveTreeBuilder.FlatEntry("2.txt", 10),
                new ArchiveTreeBuilder.FlatEntry("3.txt", 10), new ArchiveTreeBuilder.FlatEntry("4.txt", 10),
                new ArchiveTreeBuilder.FlatEntry("5.txt", 10), new ArchiveTreeBuilder.FlatEntry("6.txt", 10)
        );
        when(zipProcessingService.extractFlatEntries(any(File.class))).thenReturn(mockEntries);

        assertThrows(LimitExceededException.class, () -> archiveService.uploadArchive(multipartFile));
        verify(fileStorageService, times(1)).delete("uuid.zip"); // Ensure cleanup happened
    }

    @Test
    void getArchiveTree_ThrowsException_WhenIdUnknown() {
        when(archiveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> archiveService.getArchiveTree(99L));
    }

    @Test
    void deleteArchive_Success() {
        Archive archive = new Archive();
        archive.setStoredFileName("uuid.zip");
        when(archiveRepository.findById(1L)).thenReturn(Optional.of(archive));

        archiveService.deleteArchive(1L);

        verify(fileStorageService, times(1)).delete("uuid.zip");
        verify(archiveRepository, times(1)).delete(archive);
    }
}