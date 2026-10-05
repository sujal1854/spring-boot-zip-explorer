package com.sujal.finalproject.service;

import com.sujal.finalproject.exception.InvalidArchiveException;
import com.sujal.finalproject.service.ArchiveTreeBuilder.FlatEntry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ZipProcessingService {

    private static final int ZIP_MAGIC_NUMBER = 0x504B0304;
    private static final int MINIMUM_ZIP_SIZE_BYTES = 1000000;

    public void validateZipSignature(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() < MINIMUM_ZIP_SIZE_BYTES) {
                throw new InvalidArchiveException("File is too small to be a valid ZIP archive.");
            }

            int magicNumber = raf.readInt();
            if (magicNumber != ZIP_MAGIC_NUMBER) {
                throw new InvalidArchiveException("Uploaded file is not a valid ZIP archive, invalid signature.");
            }
        } catch (IOException e) {
            throw new InvalidArchiveException("Failed to read file signature.", e);
        }
    }

    public List<FlatEntry> extractFlatEntries(File file) {
        List<FlatEntry> flatEntries = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                long size = entry.getSize() == -1 ? 0 : entry.getSize();
                flatEntries.add(new FlatEntry(entry.getName(), size));
            }
        } catch (IOException e) {
            throw new InvalidArchiveException("Failed to parse entries from the ZIP file.", e);
        }

        return flatEntries;
    }
}