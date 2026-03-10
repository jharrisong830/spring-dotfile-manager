package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
}
