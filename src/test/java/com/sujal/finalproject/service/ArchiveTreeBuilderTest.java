package com.sujal.finalproject.service;

import com.sujal.finalproject.model.dto.ArchiveNode;
import com.sujal.finalproject.service.ArchiveTreeBuilder.FlatEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveTreeBuilderTest {

    @Test
    void testSingleFileAtRoot() {
        List<FlatEntry> entries = List.of(new FlatEntry("single.txt", 100));
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals(1, root.getChildren().size());
        ArchiveNode file = root.getChildren().get(0);
        assertEquals("single.txt", file.getName());
        assertEquals("single.txt", file.getPath());
        assertEquals("FILE", file.getType());
        assertEquals(100L, file.getSizeBytes());
    }

    @Test
    void testDeeplyNestedFileWithoutFolderEntries() {
        List<FlatEntry> entries = List.of(new FlatEntry("a/b/c/deep.txt", 50));
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        ArchiveNode folderA = root.getChildren().get(0);
        assertEquals("a", folderA.getName());
        assertEquals("FOLDER", folderA.getType());

        ArchiveNode folderB = folderA.getChildren().get(0);
        assertEquals("b", folderB.getName());

        ArchiveNode folderC = folderB.getChildren().get(0);
        assertEquals("c", folderC.getName());

        ArchiveNode file = folderC.getChildren().get(0);
        assertEquals("deep.txt", file.getName());
        assertEquals("FILE", file.getType());
    }

    @Test
    void testSharedParentFoldersNotDuplicated() {
        List<FlatEntry> entries = List.of(
                new FlatEntry("docs/file1.txt", 10),
                new FlatEntry("docs/file2.txt", 20)
        );
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals(1, root.getChildren().size());
        assertEquals(2, root.getChildren().get(0).getChildren().size());
    }

    @Test
    void testDeclaredEmptyFolderSurvives() {
        List<FlatEntry> entries = List.of(new FlatEntry("empty-folder/", 0));
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals(1, root.getChildren().size());
        assertEquals("empty-folder", root.getChildren().get(0).getName());
        assertEquals("FOLDER", root.getChildren().get(0).getType());
    }

    @Test
    void testDeclaredAndInferredFolderNotDuplicated() {
        List<FlatEntry> entries = List.of(
                new FlatEntry("docs/", 0),
                new FlatEntry("docs/readme.txt", 15)
        );
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals(1, root.getChildren().size(), "Should only have one 'docs' folder");
        assertEquals(1, root.getChildren().get(0).getChildren().size(), "Folder should contain one file");
    }

    @Test
    void testSameNameInDifferentFolders() {
        List<FlatEntry> entries = List.of(
                new FlatEntry("folder1/index.html", 5),
                new FlatEntry("folder2/index.html", 10)
        );
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals(2, root.getChildren().size());
        assertEquals("index.html", root.getChildren().get(0).getChildren().get(0).getName());
        assertEquals("index.html", root.getChildren().get(1).getChildren().get(0).getName());
    }

    @Test
    void testSpacesAndNonEnglishCharacters() {
        List<FlatEntry> entries = List.of(new FlatEntry("résumé 2024.pdf", 100));
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals("résumé 2024.pdf", root.getChildren().get(0).getName());
    }

    @Test
    void testFilesWithNoExtension() {
        List<FlatEntry> entries = List.of(new FlatEntry("dockerfile", 30));
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        assertEquals("dockerfile", root.getChildren().get(0).getName());
        assertEquals("FILE", root.getChildren().get(0).getType());
    }

    @Test
    void testBackslashesAndTrailingSlashesNormalized() {
        List<FlatEntry> entries = List.of(new FlatEntry("win\\folder\\file.txt", 10));
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        ArchiveNode winFolder = root.getChildren().get(0);
        assertEquals("win", winFolder.getName());
        assertEquals("win/folder", winFolder.getChildren().get(0).getPath());
    }

    @Test
    void testChildrenSortedCorrectly() {
        List<FlatEntry> entries = List.of(
                new FlatEntry("zebra.txt", 10),
                new FlatEntry("apple.txt", 10),
                new FlatEntry("folderB/", 0),
                new FlatEntry("folderA/", 0)
        );
        ArchiveNode root = ArchiveTreeBuilder.buildTree("root.zip", entries);

        List<ArchiveNode> children = root.getChildren();
        assertEquals("folderA", children.get(0).getName());
        assertEquals("folderB", children.get(1).getName());
        assertEquals("apple.txt", children.get(2).getName());
        assertEquals("zebra.txt", children.get(3).getName());
    }
}