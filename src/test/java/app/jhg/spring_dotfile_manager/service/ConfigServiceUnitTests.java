package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceUnitTests {

    @Mock
    private FileService fileService;

    private ConfigService configService;

    private static final String CONFIG_PATH = "/tmp/test-sdfm/config.yaml";
    private static final String REPO_PATH = "~/dotfiles";

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(CONFIG_PATH, fileService);
    }

    @Test
    public void testInitializeConfig_success() throws IOException {
        assertDoesNotThrow(() -> configService.initializeConfig(REPO_PATH));

        verify(fileService).createDirectories(eq(Path.of(CONFIG_PATH).getParent()));
        verify(fileService).writeFile(eq(Path.of(CONFIG_PATH)), eq(new SDFMConfigModel(REPO_PATH).getConfigFileContents()));
    }

    @Test
    public void testInitializeConfig_configAlreadyExists() throws IOException {
        doThrow(new FileAlreadyExistsException("File already exists"))
            .when(fileService).writeFile(any(Path.class), any(String.class));

        assertThrows(FileAlreadyExistsException.class, () -> configService.initializeConfig(REPO_PATH));
    }

    @Test
    public void testInitializeConfig_ioErrorOnCreateDirectories() throws IOException {
        doThrow(new IOException("Permission denied"))
            .when(fileService).createDirectories(any(Path.class));

        assertThrows(IOException.class, () -> configService.initializeConfig(REPO_PATH));
    }

    @Test
    public void testInitializeConfig_ioErrorOnWrite() throws IOException {
        doThrow(new IOException("Disk full"))
            .when(fileService).writeFile(any(Path.class), any(String.class));

        assertThrows(IOException.class, () -> configService.initializeConfig(REPO_PATH));
    }


    @Test
    public void testReadConfig_success() throws IOException {
        String configContent = new SDFMConfigModel(REPO_PATH).getConfigFileContents();
        when(fileService.readFile(any(Path.class))).thenReturn(configContent);
        assertEquals(REPO_PATH, configService.readConfig());
    }

    @Test
    public void testReadConfig_ioError() throws IOException {
        doThrow(new IOException("File not found"))
            .when(fileService).readFile(any(Path.class));
        assertThrows(IOException.class, () -> configService.readConfig());
    }

    @Test
    public void testReadConfig_invalidConfigFormat() throws IOException {
        when(fileService.readFile(any(Path.class))).thenReturn("invalid config content");
        assertThrows(IllegalArgumentException.class, () -> configService.readConfig());
    }

    @Test
    public void testReadConfig_missingKey() throws IOException {
        when(fileService.readFile(any(Path.class))).thenReturn("other-key: some-value");
        assertThrows(IllegalArgumentException.class, () -> configService.readConfig());
    }


    @Test 
    public void testUpdateConfig_success() throws IOException {
        when(fileService.exists(any(Path.class))).thenReturn(true);
        String newRepoPath = "~/new-dotfiles";
        assertDoesNotThrow(() -> configService.updateConfig(newRepoPath));
        verify(fileService).overwriteFile(eq(Path.of(CONFIG_PATH)), eq(new SDFMConfigModel(newRepoPath).getConfigFileContents()));
    }

    @Test
    public void testUpdateConfig_ioError() throws IOException {
        when(fileService.exists(any(Path.class))).thenReturn(true);
        doThrow(new IOException("Disk full"))
            .when(fileService).overwriteFile(any(Path.class), any(String.class));

        assertThrows(IOException.class, () -> configService.updateConfig("~/new-dotfiles"));
    }

    @Test
    public void testUpdateConfig_configFileDoesNotExist() {
        when(fileService.exists(any(Path.class))).thenReturn(false);
        assertThrows(IOException.class, () -> configService.updateConfig("~/new-dotfiles"));
    }
}
