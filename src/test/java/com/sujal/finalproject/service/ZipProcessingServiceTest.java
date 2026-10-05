package com.sujal.finalproject.service;

import com.sujal.finalproject.exception.InvalidArchiveException;
import com.sujal.finalproject.service.ArchiveTreeBuilder.FlatEntry;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZipProcessingServiceTest {

    private final ZipProcessingService zipProcessingService = new ZipProcessingService();

    @Test
    void testValidZipFileIsAcceptedAndExtracted(@TempDir Path tempDir) throws Exception {
        // 1. Build the test input dynamically in code
        File testZip = tempDir.resolve("test-archive.zip").toFile();
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(testZip))) {
            ZipEntry entry1 = new ZipEntry("folder/file1.txt");
            zout.putNextEntry(entry1);
            zout.write("Hello World".getBytes());
            zout.closeEntry();
        }

        // 2. Test Validation (Should not throw exception)
        assertDoesNotThrow(() -> zipProcessingService.validateZipSignature(testZip));

        // 3. Test Extraction
        List<FlatEntry> entries = zipProcessingService.extractFlatEntries(testZip);
        assertEquals(1, entries.size());
        assertEquals("folder/file1.txt", entries.get(0).path());
    }

    @Test
    void testInvalidFileThrowsException(@TempDir Path tempDir) throws Exception {
        // 1. Create a fake text file that is NOT a zip
        Path fakeZipPath = tempDir.resolve("fake.zip");
        Files.writeString(fakeZipPath, "This is just a text file, not a ZIP.");
        File fakeZip = fakeZipPath.toFile();

        // 2. Expect the custom exception because the magic number will fail
        InvalidArchiveException exception = assertThrows(InvalidArchiveException.class, () -> {
            zipProcessingService.validateZipSignature(fakeZip);
        });

        assertTrue(exception.getMessage().contains("invalid signature"));
    }

    @Test
    void testEmptyOrTinyFileThrowsException(@TempDir Path tempDir) throws Exception {
        Path tinyFilePath = tempDir.resolve("tiny.zip");
        Files.writeString(tinyFilePath, "123"); // Only 3 bytes
        File tinyFile = tinyFilePath.toFile();

        InvalidArchiveException exception = assertThrows(InvalidArchiveException.class, () -> {
            zipProcessingService.validateZipSignature(tinyFile);
        });

        assertTrue(exception.getMessage().contains("too small"));
    }

    @Test
    @Disabled("Waste test for local manual verification")
    void testRealFileOnMyLaptop() {
        // Swap this path out for the actual location of the ZIP on your machine
        File realZip = new File("C:\\Users\\z005brwr\\SpringBoot\\something.zip");

        // 1. Verify the ZIP signature
        assertDoesNotThrow(() -> zipProcessingService.validateZipSignature(realZip));

        // 2. Extract and print the entries so you can actually see them in your console
        List<FlatEntry> entries = zipProcessingService.extractFlatEntries(realZip);

        System.out.println("Found " + entries.size() + " entries in the real ZIP:");
        for (FlatEntry entry : entries) {
            System.out.println("- " + entry.path() + " (Size: " + entry.size() + " bytes)");
        }
    }
}