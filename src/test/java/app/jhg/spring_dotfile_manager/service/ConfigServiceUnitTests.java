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

import app.jhg.spring_dotfile_manager.config.DotfileRepoPathMixin;
import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceUnitTests {

    @Mock
    private FileService fileService;

    @Mock
    private DotfileRepoPathMixin dotfileRepoPathMixin;

    private ConfigService configService;

    private static final String CONFIG_PATH = "/tmp/test-sdfm/config.yaml";
    private static final String TILDE_CONFIG_PATH = "~/test-sdfm/config.yaml";
    private static final String REPO_PATH = "~/dotfiles";

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
    }

    @Test
    public void testConstructor_expandsTildeInConfigPath() {
        ConfigService service = new ConfigServiceImpl(TILDE_CONFIG_PATH, TILDE_CONFIG_PATH, TILDE_CONFIG_PATH, fileService, dotfileRepoPathMixin);
        Path expectedPath = Path.of(System.getProperty("user.home"), "test-sdfm/config.yaml");
        assertEquals(expectedPath, ((ConfigServiceImpl) service).getConfigFilePath());
    }

    @Test
    public void testConstructor_manualPathProvided_configFilePathUnchanged() {
        ConfigService service = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
        
        // Config file path should always be CONFIG_PATH, regardless of manual dotfile repo path
        Path expectedPath = Path.of(CONFIG_PATH);
        assertEquals(expectedPath, ((ConfigServiceImpl) service).getConfigFilePath());
    }

    @Test
    public void testReadConfig_manualPathProvided_returnsManualPath() throws IOException {
        String manualPath = "/custom/dotfiles/path";
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(manualPath);
        
        ConfigService service = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
        
        // Should return manual path without reading config file
        String result = service.readConfig();
        assertEquals(manualPath, result);
        verify(fileService, never()).readFile(any());
    }

    @Test
    public void testReadConfig_manualPathWithTilde_returnsPathAsIs() throws IOException {
        String manualPath = "~/manual/dotfiles";
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(manualPath);
        
        ConfigService service = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
        
        // Manual path should be returned as-is (tilde expansion happens in calling code if needed)
        String result = service.readConfig();
        assertEquals(manualPath, result);
        verify(fileService, never()).readFile(any());
    }

    @Test
    public void testReadConfig_manualPathIsBlank_readsFromConfigFile() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn("   ");
        String configContent = new SDFMConfigModel(REPO_PATH).getConfigFileContents();
        when(fileService.readFile(any(Path.class))).thenReturn(configContent);
        
        ConfigService service = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
        
        String result = service.readConfig();
        assertEquals(REPO_PATH, result);
        verify(fileService).readFile(Path.of(CONFIG_PATH));
    }

    @Test
    public void testReadConfig_manualPathIsEmpty_readsFromConfigFile() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn("");
        String configContent = new SDFMConfigModel(REPO_PATH).getConfigFileContents();
        when(fileService.readFile(any(Path.class))).thenReturn(configContent);
        
        ConfigService service = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
        
        String result = service.readConfig();
        assertEquals(REPO_PATH, result);
        verify(fileService).readFile(Path.of(CONFIG_PATH));
    }

    @Test
    public void testReadConfig_manualPathNull_readsFromConfigFile() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(null);
        String configContent = new SDFMConfigModel(REPO_PATH).getConfigFileContents();
        when(fileService.readFile(any(Path.class))).thenReturn(configContent);
        
        ConfigService service = new ConfigServiceImpl(CONFIG_PATH, CONFIG_PATH, CONFIG_PATH, fileService, dotfileRepoPathMixin);
        
        String result = service.readConfig();
        assertEquals(REPO_PATH, result);
        verify(fileService).readFile(Path.of(CONFIG_PATH));
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
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(null);
        String configContent = new SDFMConfigModel(REPO_PATH).getConfigFileContents();
        when(fileService.readFile(any(Path.class))).thenReturn(configContent);
        assertEquals(REPO_PATH, configService.readConfig());
    }

    @Test
    public void testReadConfig_ioError() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(null);
        doThrow(new IOException("File not found"))
            .when(fileService).readFile(any(Path.class));
        assertThrows(IOException.class, () -> configService.readConfig());
    }

    @Test
    public void testReadConfig_invalidConfigFormat() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(null);
        when(fileService.readFile(any(Path.class))).thenReturn("invalid config content");
        assertThrows(IllegalArgumentException.class, () -> configService.readConfig());
    }

    @Test
    public void testReadConfig_missingKey() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(null);
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

    @Test
    public void testUpdateConfig_emptyPath_doesNotValidate() throws IOException {
        // ConfigServiceImpl does not validate the path; that is the command layer's responsibility.
        // An empty string is passed through and written to the config file as-is.
        when(fileService.exists(any(Path.class))).thenReturn(true);
        assertDoesNotThrow(() -> configService.updateConfig(""));
        verify(fileService).overwriteFile(eq(Path.of(CONFIG_PATH)), eq(new SDFMConfigModel("").getConfigFileContents()));
    }

    @Test
    public void testReadConfig_emptyPath_throwsIllegalArgumentException() throws IOException {
        when(dotfileRepoPathMixin.getDotfileRepoPath()).thenReturn(null);
        when(fileService.readFile(any(Path.class))).thenReturn(new SDFMConfigModel("").getConfigFileContents());

        assertThrows(IllegalArgumentException.class, () -> configService.readConfig());
    }
}
