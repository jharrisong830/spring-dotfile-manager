package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;

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

    private DotfileService dotfileService;

    private static final String GLOB_PATTERN = "**/*.dotfile";
    private static final String RAW_REPO_PATH = "~/dotfiles";
    private static final String RESOLVED_REPO_PATH = System.getProperty("user.home") + "/dotfiles";

    @BeforeEach
    void setUp() {
        dotfileService = new DotfileServiceImpl(GLOB_PATTERN, configService, fileService);
    }

    @Test
    public void testGetAllDotfileMarkerPaths_success() throws IOException {
        List<Path> expectedPaths = List.of(
            Path.of(RESOLVED_REPO_PATH, "foo.dotfile"),
            Path.of(RESOLVED_REPO_PATH, "bar/baz.dotfile")
        );

        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
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
        doThrow(new IOException("Repo directory does not exist"))
            .when(fileService).glob(any(Path.class), anyString());

        assertThrows(IOException.class, () -> dotfileService.getAllDotfileMarkerPaths());
    }


    @Test
    public void testGetDotfileMarkerModelsByPath_singleMarker() throws IOException {
        Path markerPath = Path.of(RESOLVED_REPO_PATH, "zshrc.dotfile");
        Path expectedSource = Path.of(RESOLVED_REPO_PATH, ".zshrc");
        String rawContent = "name: .zshrc\nlocation: ~/.zshrc\n";

        when(fileService.readFile(markerPath)).thenReturn(rawContent);

        List<DotfileMarkerModel> result = dotfileService.getDotfileMarkerModelsByPath(markerPath);

        assertEquals(1, result.size());
        assertEquals(".zshrc", result.get(0).name);
        assertEquals(Path.of(System.getProperty("user.home"), ".zshrc"), result.get(0).location);
        assertEquals(expectedSource, result.get(0).sourceLocation);
    }

    @Test
    public void testGetDotfileMarkerModelsByPath_multipleMarkers() throws IOException {
        Path markerPath = Path.of(RESOLVED_REPO_PATH, "shell.dotfile");
        Path expectedSourceZsh = Path.of(RESOLVED_REPO_PATH, ".zshrc");
        Path expectedSourceBash = Path.of(RESOLVED_REPO_PATH, ".bashrc");

        String rawContent = "name: .zshrc\nlocation: ~/.zshrc\n---\nname: .bashrc\nlocation: ~/.bashrc\n";

        when(fileService.readFile(markerPath)).thenReturn(rawContent);

        List<DotfileMarkerModel> result = dotfileService.getDotfileMarkerModelsByPath(markerPath);

        assertEquals(2, result.size());
        assertEquals(".zshrc", result.get(0).name);
        assertEquals(expectedSourceZsh, result.get(0).sourceLocation);
        assertEquals(".bashrc", result.get(1).name);
        assertEquals(Path.of(System.getProperty("user.home"), ".bashrc"), result.get(1).location);
        assertEquals(expectedSourceBash, result.get(1).sourceLocation);
    }

    @Test
    public void testGetDotfileMarkerModelsByPath_readFileThrowsIOException() throws IOException {
        Path markerPath = Path.of(RESOLVED_REPO_PATH, "zshrc.dotfile");

        doThrow(new IOException("File not found"))
            .when(fileService).readFile(markerPath);

        assertThrows(IOException.class, () -> dotfileService.getDotfileMarkerModelsByPath(markerPath));
    }

    @Test
    public void testGetDotfileMarkerModelsByPath_invalidFileContent() throws IOException {
        Path markerPath = Path.of(RESOLVED_REPO_PATH, "zshrc.dotfile");
        String rawContent = "not valid marker content";

        when(fileService.readFile(markerPath)).thenReturn(rawContent);

        assertThrows(IllegalArgumentException.class, () -> dotfileService.getDotfileMarkerModelsByPath(markerPath));
    }


    @Test
    public void testGetAllDotfileMarkerModels_emptyPathList() throws IOException {
        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(fileService.glob(eq(Path.of(RESOLVED_REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of());

        List<DotfileMarkerModel> result = dotfileService.getAllDotfileMarkerModels();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllDotfileMarkerModels_singlePathSingleMarker() throws IOException {
        Path markerPath = Path.of(RESOLVED_REPO_PATH, "zshrc.dotfile");
        String rawContent = "name: .zshrc\nlocation: ~/.zshrc\n";

        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(fileService.glob(eq(Path.of(RESOLVED_REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of(markerPath));
        when(fileService.readFile(markerPath)).thenReturn(rawContent);

        List<DotfileMarkerModel> result = dotfileService.getAllDotfileMarkerModels();

        assertEquals(1, result.size());
        assertEquals(".zshrc", result.get(0).name);
    }

    @Test
    public void testGetAllDotfileMarkerModels_multiplePathsMultipleMarkersFlattened() throws IOException {
        Path pathA = Path.of(RESOLVED_REPO_PATH, "shell.dotfile");
        Path pathB = Path.of(RESOLVED_REPO_PATH, "editors.dotfile");
        String rawA = "name: .zshrc\nlocation: ~/.zshrc\n---\nname: .bashrc\nlocation: ~/.bashrc\n";
        String rawB = "name: .vimrc\nlocation: ~/.vimrc\n";

        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(fileService.glob(eq(Path.of(RESOLVED_REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of(pathA, pathB));
        when(fileService.readFile(pathA)).thenReturn(rawA);
        when(fileService.readFile(pathB)).thenReturn(rawB);

        List<DotfileMarkerModel> result = dotfileService.getAllDotfileMarkerModels();

        assertEquals(3, result.size());
        assertEquals(".zshrc", result.get(0).name);
        assertEquals(".bashrc", result.get(1).name);
        assertEquals(".vimrc", result.get(2).name);
    }

    @Test
    public void testGetAllDotfileMarkerModels_illegalArgumentExceptionMidIteration_propagates() throws IOException {
        Path pathA = Path.of(RESOLVED_REPO_PATH, "zshrc.dotfile");
        Path pathB = Path.of(RESOLVED_REPO_PATH, "bad.dotfile");

        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(fileService.glob(eq(Path.of(RESOLVED_REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of(pathA, pathB));
        when(fileService.readFile(pathA)).thenReturn("name: .zshrc\nlocation: ~/.zshrc\n");
        when(fileService.readFile(pathB)).thenReturn("not valid marker content");

        assertThrows(IllegalArgumentException.class, () -> dotfileService.getAllDotfileMarkerModels());
    }

    @Test
    public void testGetAllDotfileMarkerModels_getAllDotfileMarkerPathsThrowsIOException() throws IOException {
        doThrow(new IOException("Config file not found"))
            .when(configService).readConfig();

        assertThrows(IOException.class, () -> dotfileService.getAllDotfileMarkerModels());
    }

    @Test
    public void testGetAllDotfileMarkerModels_getDotfileMarkerModelsByPathThrowsIOException() throws IOException {
        Path markerPath = Path.of(RESOLVED_REPO_PATH, "zshrc.dotfile");

        when(configService.readConfig()).thenReturn(RAW_REPO_PATH);
        when(fileService.glob(eq(Path.of(RESOLVED_REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of(markerPath));
        doThrow(new IOException("File not found"))
            .when(fileService).readFile(markerPath);

        assertThrows(IOException.class, () -> dotfileService.getAllDotfileMarkerModels());
    }
}
