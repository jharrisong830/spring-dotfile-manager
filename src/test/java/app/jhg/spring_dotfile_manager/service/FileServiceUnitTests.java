package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileServiceUnitTests {

    private final FileService fileService = new FileServiceImpl();

    @TempDir
    Path tempDir;

    @Test
    public void testExists_fileExists() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        Files.createFile(filePath);

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
        Files.createDirectory(dirPath);

        assertTrue(fileService.exists(dirPath));
    }


    @Test
    public void testIsDirectory_isDirectory() throws IOException {
        Path dirPath = tempDir.resolve("testDir");
        Files.createDirectory(dirPath);

        assertTrue(fileService.isDirectory(dirPath));
    }

    @Test
    public void testIsDirectory_isNotDirectory() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        Files.createFile(filePath);

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
        Files.createFile(filePath);

        assertFalse(fileService.isSymbolicLink(filePath));
    }

    @Test
    public void testIsSymbolicLink_doesNotExist() {
        Path filePath = tempDir.resolve("nonExistentFile.txt");

        assertFalse(fileService.isSymbolicLink(filePath));
    }


    @Test
    public void testCreateDirectories_directoryDoesNotExist() {
        Path dirPath = tempDir.resolve("newDir");

        assertDoesNotThrow(() -> fileService.createDirectories(dirPath));
        assertTrue(Files.exists(dirPath));
        assertTrue(Files.isDirectory(dirPath));
    }

    @Test
    public void testCreateDirectories_directoryAlreadyExists() throws IOException {
        Path dirPath = tempDir.resolve("existingDir");
        Files.createDirectory(dirPath);

        assertDoesNotThrow(() -> fileService.createDirectories(dirPath));
    }

    @Test
    public void testCreateDirectories_nestedDirectories() {
        Path dirPath = tempDir.resolve("a/b/c");

        assertDoesNotThrow(() -> fileService.createDirectories(dirPath));
        assertTrue(Files.exists(dirPath));
        assertTrue(Files.isDirectory(dirPath));
    }


    @Test
    public void testWriteFile_fileDoesNotExist() {
        Path filePath = tempDir.resolve("testFile.txt");

        assertDoesNotThrow(() -> fileService.writeFile(filePath, "Test content"));
        assertTrue(Files.exists(filePath));
        assertDoesNotThrow(() -> assertEquals("Test content", Files.readString(filePath)));
    }

    @Test
    public void testWriteFile_parentDirectoryDoesNotExist() {
        Path filePath = tempDir.resolve("nonExistentDir/testFile.txt");

        assertThrows(IOException.class, () -> fileService.writeFile(filePath, "Test content"));
    }

    @Test
    public void testWriteFile_fileAlreadyExists() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        Files.createFile(filePath);

        assertThrows(FileAlreadyExistsException.class, () -> fileService.writeFile(filePath, "Test content"));
    }


    @Test
    public void testReadFile_fileExists() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        String content = "Test content";
        Files.writeString(filePath, content);

        String readContent = fileService.readFile(filePath);
        assertEquals(content, readContent);
    }

    @Test
    public void testReadFile_fileDoesNotExist() {
        Path filePath = tempDir.resolve("nonExistentFile.txt");
        assertThrows(IOException.class, () -> fileService.readFile(filePath));
    }

    @Test
    public void testReadFile_isDirectory() throws IOException {
        Path dirPath = tempDir.resolve("testDir");
        Files.createDirectory(dirPath);

        assertThrows(IOException.class, () -> fileService.readFile(dirPath));
    }

    @Test
    public void testReadFile_isSymbolicLink() throws IOException {
        Path targetFile = tempDir.resolve("targetFile.txt");
        String content = "Test content";
        Files.writeString(targetFile, content);
        Path symbolicLink = tempDir.resolve("symbolicLink.txt");
        Files.createSymbolicLink(symbolicLink, targetFile);

        String readContent = fileService.readFile(symbolicLink);
        assertEquals(content, readContent);
    }
}
