package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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


    @Test
    public void testOverwriteFile_replacesExistingContent() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        Files.writeString(filePath, "original content");

        fileService.overwriteFile(filePath, "new content");

        assertEquals("new content", Files.readString(filePath));
    }

    @Test
    public void testOverwriteFile_createsFileIfAbsent() throws IOException {
        Path filePath = tempDir.resolve("newFile.txt");

        assertDoesNotThrow(() -> fileService.overwriteFile(filePath, "content"));
        assertEquals("content", Files.readString(filePath));
    }


    @Test
    public void testCreateSymlink_toFile() throws IOException {
        Path target = tempDir.resolve("target.txt");
        Files.createFile(target);
        Path link = tempDir.resolve("link.txt");

        fileService.createSymlink(link, target);

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
    }

    @Test
    public void testCreateSymlink_toDirectory() throws IOException {
        Path target = tempDir.resolve("targetDir");
        Files.createDirectory(target);
        Path link = tempDir.resolve("linkDir");

        fileService.createSymlink(link, target);

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(target, Files.readSymbolicLink(link));
    }

    @Test
    public void testCreateSymlink_linkAlreadyExists() throws IOException {
        Path target = tempDir.resolve("target.txt");
        Files.createFile(target);
        Path link = tempDir.resolve("link.txt");
        Files.createFile(link);

        assertThrows(FileAlreadyExistsException.class, () -> fileService.createSymlink(link, target));
    }

    @Test
    public void testCreateSymlink_parentDirectoryDoesNotExist() throws IOException {
        Path target = tempDir.resolve("target.txt");
        Files.createFile(target);
        Path link = tempDir.resolve("nonExistentDir/link.txt");

        assertThrows(IOException.class, () -> fileService.createSymlink(link, target));
    }


    @Test
    public void testDeleteFile_regularFile() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        Files.createFile(filePath);

        fileService.deleteFile(filePath);

        assertFalse(Files.exists(filePath));
    }

    @Test
    public void testDeleteFile_symbolicLink() throws IOException {
        Path targetFile = tempDir.resolve("targetFile.txt");
        Files.createFile(targetFile);
        Path symbolicLink = tempDir.resolve("symbolicLink.txt");
        Files.createSymbolicLink(symbolicLink, targetFile);

        fileService.deleteFile(symbolicLink);

        assertFalse(Files.exists(symbolicLink));
        assertTrue(Files.exists(targetFile));
    }

    @Test
    public void testDeleteFile_emptyDirectory() throws IOException {
        Path dirPath = tempDir.resolve("emptyDir");
        Files.createDirectory(dirPath);

        fileService.deleteFile(dirPath);

        assertFalse(Files.exists(dirPath));
    }

    @Test
    public void testDeleteFile_nonExistentPath() {
        Path filePath = tempDir.resolve("nonExistentFile.txt");

        assertThrows(IOException.class, () -> fileService.deleteFile(filePath));
    }

    @Test
    public void testDeleteFile_nonEmptyDirectory() throws IOException {
        Path dirPath = tempDir.resolve("nonEmptyDir");
        Files.createDirectory(dirPath);
        Files.createFile(dirPath.resolve("child.txt"));

        assertThrows(IOException.class, () -> fileService.deleteFile(dirPath));
    }


    @Test
    public void testForceDelete_regularFile() throws IOException {
        Path filePath = tempDir.resolve("testFile.txt");
        Files.createFile(filePath);

        fileService.forceDelete(filePath);

        assertFalse(Files.exists(filePath));
    }

    @Test
    public void testForceDelete_symbolicLink() throws IOException {
        Path target = tempDir.resolve("target.txt");
        Files.createFile(target);
        Path link = tempDir.resolve("link.txt");
        Files.createSymbolicLink(link, target);

        fileService.forceDelete(link);

        assertFalse(Files.exists(link));
        assertTrue(Files.exists(target));
    }

    @Test
    public void testForceDelete_emptyDirectory() throws IOException {
        Path dirPath = tempDir.resolve("emptyDir");
        Files.createDirectory(dirPath);

        fileService.forceDelete(dirPath);

        assertFalse(Files.exists(dirPath));
    }

    @Test
    public void testForceDelete_directoryWithFiles() throws IOException {
        Path dirPath = tempDir.resolve("dir");
        Files.createDirectory(dirPath);
        Path file1 = dirPath.resolve("a.txt");
        Path file2 = dirPath.resolve("b.txt");
        Files.createFile(file1);
        Files.createFile(file2);

        fileService.forceDelete(dirPath);

        assertFalse(Files.exists(dirPath));
        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
    }

    @Test
    public void testForceDelete_directoryWithNestedContents() throws IOException {
        Path dirPath = tempDir.resolve("dir");
        Path subDir = dirPath.resolve("subDir");
        Files.createDirectories(subDir);
        Path file1 = dirPath.resolve("a.txt");
        Path file2 = subDir.resolve("b.txt");
        Files.createFile(file1);
        Files.createFile(file2);

        fileService.forceDelete(dirPath);

        assertFalse(Files.exists(dirPath));
        assertFalse(Files.exists(subDir));
        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
    }


    @Test
    public void testGlob_basicMatch() throws IOException {
        Files.createFile(tempDir.resolve("a.yaml"));
        Files.createFile(tempDir.resolve("b.yaml"));
        Files.createFile(tempDir.resolve("c.txt"));

        List<Path> result = fileService.glob(tempDir, "*.yaml");

        assertEquals(2, result.size());
        assertTrue(result.contains(tempDir.resolve("a.yaml")));
        assertTrue(result.contains(tempDir.resolve("b.yaml")));
    }

    @Test
    public void testGlob_noMatches() throws IOException {
        Files.createFile(tempDir.resolve("a.txt"));
        Files.createFile(tempDir.resolve("b.txt"));

        List<Path> result = fileService.glob(tempDir, "*.yaml");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGlob_recursivePattern() throws IOException {
        Path subDir = tempDir.resolve("sub");
        Files.createDirectories(subDir);
        Files.createFile(tempDir.resolve("root.yaml"));
        Files.createFile(subDir.resolve("nested.yaml"));

        List<Path> result = fileService.glob(tempDir, "**/*.yaml");

        assertEquals(2, result.size());
        assertTrue(result.contains(tempDir.resolve("root.yaml")));
        assertTrue(result.contains(subDir.resolve("nested.yaml")));
    }

    @Test
    public void testGlob_patternExcludesNonMatchingFiles() throws IOException {
        Files.createFile(tempDir.resolve("match.yaml"));
        Files.createFile(tempDir.resolve("no-match.txt"));
        Files.createFile(tempDir.resolve("also-no-match.json"));

        List<Path> result = fileService.glob(tempDir, "*.yaml");

        assertEquals(1, result.size());
        assertTrue(result.contains(tempDir.resolve("match.yaml")));
    }

    @Test
    public void testGlob_emptyBaseDirectory() throws IOException {
        List<Path> result = fileService.glob(tempDir, "**/*.yaml");

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGlob_baseDirectoryDoesNotExist() {
        Path nonExistent = tempDir.resolve("does-not-exist");

        assertThrows(IOException.class, () -> fileService.glob(nonExistent, "*.yaml"));
    }

    @Test
    public void testGlob_baseDirectoryIsNotADirectory() throws IOException {
        Path file = tempDir.resolve("a-file.txt");
        Files.createFile(file);

        assertThrows(IOException.class, () -> fileService.glob(file, "*.yaml"));
    }
}
