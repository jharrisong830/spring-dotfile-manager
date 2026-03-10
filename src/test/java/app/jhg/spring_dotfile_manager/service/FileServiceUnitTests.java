package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;

public class FileServiceUnitTests {

    private final FileService fileService = new FileServiceImpl();

    @TempDir
    Path tempDir;

    @Test
    public void testExists_fileExists() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        filePath.toFile().createNewFile();

        assertTrue(fileService.exists(filePath));
    }

    @Test
    public void testExists_fileDoesNotExist() {
        Path filePath = tempDir.resolve("nonExistentFile.txt");

        assertFalse(fileService.exists(filePath));
    }

    @Test
    public void testExists_directoryExists() throws IOException {
        Path dirPath = tempDir.resolve("testDir");
        dirPath.toFile().mkdir();

        assertTrue(fileService.exists(dirPath));
    }


    @Test
    public void testIsDirectory_isDirectory() throws IOException {
        Path dirPath = tempDir.resolve("testDir");
        dirPath.toFile().mkdir();

        assertTrue(fileService.isDirectory(dirPath));
    }

    @Test
    public void testIsDirectory_isNotDirectory() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        filePath.toFile().createNewFile();

        assertFalse(fileService.isDirectory(filePath));
    }

    @Test
    public void testIsDirectory_doesNotExist() {
        Path filePath = tempDir.resolve("nonExistentFile.txt");

        assertFalse(fileService.isDirectory(filePath));
    }


    @Test
    public void testIsSymbolicLink_isSymbolicLink() throws IOException {
        Path targetFile = tempDir.resolve("targetFile.txt");
        targetFile.toFile().createNewFile();
        Path symbolicLink = tempDir.resolve("symbolicLink.txt");
        Files.createSymbolicLink(symbolicLink, targetFile);

        assertTrue(fileService.isSymbolicLink(symbolicLink));
    }

    @Test
    public void testIsSymbolicLink_isNotSymbolicLink() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        filePath.toFile().createNewFile();

        assertFalse(fileService.isSymbolicLink(filePath));
    }

    @Test
    public void testIsSymbolicLink_doesNotExist() {
        Path filePath = tempDir.resolve("nonExistentFile.txt");

        assertFalse(fileService.isSymbolicLink(filePath));
    }


    @Test
    public void testWriteFile_fileDoesNotExist() {
        Path filePath = tempDir.resolve("testFile.txt");

        assertDoesNotThrow(() -> fileService.writeFile(filePath, "Test content"));
        assertTrue(Files.exists(filePath)); // file should be written afterwards
    }

    @Test
    public void testWriteFile_fileAlreadyExists() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        filePath.toFile().createNewFile();

        assertThrows(FileExistsException.class, () -> fileService.writeFile(filePath, "Test content"));
    }
}
