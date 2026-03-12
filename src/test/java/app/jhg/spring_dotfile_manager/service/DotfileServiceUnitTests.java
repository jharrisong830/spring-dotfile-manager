package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DotfileServiceUnitTests {

    @Mock
    private ConfigService configService;

    @Mock
    private FileService fileService;

    @Mock
    private FormatterService formatterService;

    private DotfileService dotfileService;

    private static final String GLOB_PATTERN = "**/*.dotfile";
    private static final String RAW_REPO_PATH = "~/dotfiles";
    private static final String RESOLVED_REPO_PATH = "/home/user/dotfiles";

    @BeforeEach
    void setUp() {
        dotfileService = new DotfileServiceImpl(GLOB_PATTERN, configService, fileService, formatterService);
    }

    @Test
    public void testGetAllDotfileMarkerPaths_success() throws IOException {
        List<Path> expectedPaths = List.of(
            Path.of(RESOLVED_REPO_PATH, "foo.dotfile"),
            Path.of(RESOLVED_REPO_PATH, "bar/baz.dotfile")
        );

        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(formatterService.formatWithHomeDirectory(RAW_REPO_PATH)).thenReturn(RESOLVED_REPO_PATH);
        when(fileService.glob(eq(Path.of(RESOLVED_REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(expectedPaths);

        List<Path> result = dotfileService.getAllDotfileMarkerPaths();

        assertEquals(expectedPaths, result);
    }

    @Test
    public void testGetAllDotfileMarkerPaths_readConfigThrowsIOException() throws IOException {
        doThrow(new IOException("Config file not found"))
            .when(configService).readConfig();

        assertThrows(IOException.class, () -> dotfileService.getAllDotfileMarkerPaths());

        verify(fileService, never()).glob(any(), anyString());
    }

    @Test
    public void testGetAllDotfileMarkerPaths_globThrowsIOException() throws IOException {
        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(formatterService.formatWithHomeDirectory(RAW_REPO_PATH)).thenReturn(RESOLVED_REPO_PATH);
        doThrow(new IOException("Repo directory does not exist"))
            .when(fileService).glob(any(Path.class), anyString());

        assertThrows(IOException.class, () -> dotfileService.getAllDotfileMarkerPaths());
    }
}
