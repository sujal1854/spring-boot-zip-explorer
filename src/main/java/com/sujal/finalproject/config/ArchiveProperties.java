package com.sujal.finalproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "archive")
public class ArchiveProperties {

    private String storageDir = "./data/archives";
    private long maxUploadSizeBytes = 104857600;
    private int maxEntryCount = 5000;
    private long maxUncompressedSizeBytes = 524288000;

    public String getStorageDir() { return storageDir; }
    public void setStorageDir(String storageDir) { this.storageDir = storageDir; }

    public long getMaxUploadSizeBytes() { return maxUploadSizeBytes; }
    public void setMaxUploadSizeBytes(long maxUploadSizeBytes) { this.maxUploadSizeBytes = maxUploadSizeBytes; }

    public int getMaxEntryCount() { return maxEntryCount; }
    public void setMaxEntryCount(int maxEntryCount) { this.maxEntryCount = maxEntryCount; }

    public long getMaxUncompressedSizeBytes() { return maxUncompressedSizeBytes; }
    public void setMaxUncompressedSizeBytes(long maxUncompressedSizeBytes) { this.maxUncompressedSizeBytes = maxUncompressedSizeBytes; }
}