package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;
import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceUnitTests {

    @Mock
    private FileService fileService;

    private ConfigServiceImpl configService;

    private static final String CONFIG_PATH = "/tmp/test-sdfm/config.yaml";
    private static final String REPO_PATH = "~/dotfiles";

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(CONFIG_PATH, fileService);
    }

    @Test
    public void testInitializeConfig_success() throws FileExistsException, IOException {
        assertDoesNotThrow(() -> configService.initializeConfig(REPO_PATH));

        verify(fileService).createDirectories(eq(Path.of(CONFIG_PATH).getParent()));
        verify(fileService).writeFile(eq(Path.of(CONFIG_PATH)), eq(new SDFMConfigModel(REPO_PATH).getConfigFileContents()));
    }

    @Test
    public void testInitializeConfig_configAlreadyExists() throws FileExistsException, IOException {
        doThrow(new FileExistsException("File already exists"))
            .when(fileService).writeFile(any(Path.class), any(String.class));

        assertThrows(FileExistsException.class, () -> configService.initializeConfig(REPO_PATH));
    }

    @Test
    public void testInitializeConfig_ioErrorOnCreateDirectories() throws IOException {
        doThrow(new IOException("Permission denied"))
            .when(fileService).createDirectories(any(Path.class));

        assertThrows(IOException.class, () -> configService.initializeConfig(REPO_PATH));
    }

    @Test
    public void testInitializeConfig_ioErrorOnWrite() throws FileExistsException, IOException {
        doThrow(new IOException("Disk full"))
            .when(fileService).writeFile(any(Path.class), any(String.class));

        assertThrows(IOException.class, () -> configService.initializeConfig(REPO_PATH));
    }
}
