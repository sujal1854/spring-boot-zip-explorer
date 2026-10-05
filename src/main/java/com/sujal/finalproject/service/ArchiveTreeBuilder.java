package com.sujal.finalproject.service;

import com.sujal.finalproject.model.dto.ArchiveNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchiveTreeBuilder {

    public record FlatEntry(String path, long size) {}

    public static ArchiveNode buildTree(String archiveName, List<FlatEntry> entries) {
        // Updated root instantiation
        ArchiveNode root = new ArchiveNode(archiveName, archiveName, "FOLDER", null);

        Map<String, ArchiveNode> folderMap = new HashMap<>();
        folderMap.put("", root);

        for (FlatEntry entry : entries) {
            String rawPath = entry.path().replace("\\", "/");
            boolean isDeclaredFolder = rawPath.endsWith("/");
            String normalizedPath = isDeclaredFolder ? rawPath.substring(0, rawPath.length() - 1) : rawPath;

            if (normalizedPath.isEmpty()) continue;

            String[] parts = normalizedPath.split("/");
            ArchiveNode currentParent = root;
            String currentPath = "";

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                currentPath = currentPath.isEmpty() ? part : currentPath + "/" + part;

                boolean isLast = (i == parts.length - 1);
                boolean isFolder = !isLast || isDeclaredFolder;

                if (isFolder) {
                    ArchiveNode folder = folderMap.get(currentPath);
                    if (folder == null) {
                        folder = new ArchiveNode(part, currentPath, "FOLDER", null);
                        folderMap.put(currentPath, folder);
                        addChildToNode(currentParent, folder);
                    }
                    currentParent = folder;
                } else {
                    ArchiveNode file = new ArchiveNode(part, currentPath, "FILE", entry.size());
                    addChildToNode(currentParent, file);
                }
            }
        }

        sortTreeRecursively(root);
        return root;
    }

    private static void addChildToNode(ArchiveNode parent, ArchiveNode child) {
        if (parent.getChildren() == null) {
            parent.setChildren(new java.util.ArrayList<>());
        }

        // Prevent duplication for declared vs inferred folders
        if ("FOLDER".equals(child.getType())) {
            for (ArchiveNode existing : parent.getChildren()) {
                if (existing.getPath().equals(child.getPath())) {
                    return;
                }
            }
        }
        parent.getChildren().add(child);
    }

    private static void sortTreeRecursively(ArchiveNode node) {
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            // Sort: Folders first (false evaluates before true), then alphabetically by name
            node.getChildren().sort(Comparator
                    .comparing((ArchiveNode n) -> "FILE".equals(n.getType()))
                    .thenComparing(ArchiveNode::getName)
            );

            for (ArchiveNode child : node.getChildren()) {
                sortTreeRecursively(child);
            }
        }
    }
}