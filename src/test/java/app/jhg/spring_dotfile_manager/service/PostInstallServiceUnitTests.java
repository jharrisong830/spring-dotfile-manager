package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;
import app.jhg.spring_dotfile_manager.model.SubprocessResult;

@ExtendWith(MockitoExtension.class)
public class PostInstallServiceUnitTests {
    
    @Mock
    private ConfigService configService;

    @Mock
    private FileService fileService;

    @Mock
    private SubprocessService subprocessService;

    private PostInstallService postInstallService;

    private static final String GLOB_PATTERN = "post-install/**/*.sh";
    private static final String WIN32_GLOB_PATTERN = "post-install/**/*.ps1";
    private static final String REPO_PATH = System.getProperty("user.home") + "/dotfiles";

    @BeforeEach
    void setUp() {
        postInstallService = new PostInstallServiceImpl(GLOB_PATTERN, GLOB_PATTERN, WIN32_GLOB_PATTERN, "Linux", configService, fileService, subprocessService);
    }

    @Test
    public void testRunPostInstallScripts_success() throws Exception {
        List<Path> scriptPaths = List.of(
            Path.of(REPO_PATH, "post-install/01.sh"),
            Path.of(REPO_PATH, "post-install/02.sh"),
            Path.of(REPO_PATH, "post-install/folder/01.sh")
        );
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(scriptPaths);
        when(
            subprocessService.executeCommand(any(), any())
        ).thenReturn(new SubprocessResult(0, ""));

        List<PostInstallScriptResult> expectedResults = scriptPaths.stream().map(
            p -> new PostInstallScriptResult(true, "", p)
        ).toList();
        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();
        
        assertEquals(expectedResults, results);

        for (Path path : scriptPaths) {
            verify(subprocessService).executeCommand(eq(path.getParent()), eq(List.of("bash", path.toString())));
        }
    }

    @Test
    public void testRunPostInstallScripts_postInstallDisabled() throws Exception {
        when(configService.readAllowPostInstallScripts()).thenReturn(false); // should immediately return empty list

        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();

        assertEquals(List.of(), results);

        verifyNoInteractions(fileService);
        verifyNoInteractions(subprocessService);
        verify(configService, times(1)).readAllowPostInstallScripts();
    }

    @Test
    public void testRunPostInstallScripts_errors() throws Exception {
        List<Path> scriptPaths = List.of(
            Path.of(REPO_PATH, "post-install/01-command-error.sh"),
            Path.of(REPO_PATH, "post-install/02-exception.sh"),
            Path.of(REPO_PATH, "post-install/03-success.sh")
        );
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(scriptPaths);
        
        // different outcome per each script

        // some runtime error
        when(subprocessService.executeCommand(scriptPaths.get(0).getParent(), List.of("bash", scriptPaths.get(0).toString()))).thenReturn(new SubprocessResult(1, "command error"));

        // exception thrown
        when(subprocessService.executeCommand(scriptPaths.get(1).getParent(), List.of("bash", scriptPaths.get(1).toString()))).thenThrow(new IOException("exception thrown!"));
        
        // success
        when(subprocessService.executeCommand(scriptPaths.get(2).getParent(), List.of("bash", scriptPaths.get(2).toString()))).thenReturn(new SubprocessResult(0, ""));

        List<PostInstallScriptResult> expectedResults = List.of(
            new PostInstallScriptResult(false, "command error", scriptPaths.get(0)),
            new PostInstallScriptResult(false, "Failed to run post-install script: " + "exception thrown!", scriptPaths.get(1)),
            new PostInstallScriptResult(true, "", scriptPaths.get(2))
        );
        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();
        
        assertEquals(expectedResults, results);
    }

    @Test
    public void testRunPostInstallScripts_sortsScriptsBeforeRunning() throws Exception {
        Path scriptB = Path.of(REPO_PATH, "post-install/02.sh");
        Path scriptA = Path.of(REPO_PATH, "post-install/01.sh");
        Path scriptC = Path.of(REPO_PATH, "post-install/folder/01.sh");
        
        // out of order
        List<Path> scriptPaths = List.of(scriptC, scriptB, scriptA);

        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(scriptPaths);
        when(subprocessService.executeCommand(any(), any())).thenReturn(new SubprocessResult(0, ""));

        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();

        assertEquals(List.of(scriptA, scriptB, scriptC), results.stream().map(PostInstallScriptResult::script).toList());
    }

    @Test
    public void testRunPostInstallScripts_readDotfileRepoPathThrowsIOException_propagates() throws Exception {
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        doThrow(new IOException("could not read config")).when(configService).readDotfileRepoPath();

        assertThrows(IOException.class, postInstallService::runPostInstallScripts);

        verifyNoInteractions(fileService);
        verifyNoInteractions(subprocessService);
    }

    @Test
    public void testRunPostInstallScripts_globThrowsIOException_propagates() throws Exception {
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        doThrow(new IOException("base directory does not exist"))
            .when(fileService).glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN));

        assertThrows(IOException.class, postInstallService::runPostInstallScripts);

        verifyNoInteractions(subprocessService);
    }

    @Test
    public void testRunPostInstallScripts_noScriptsFound_returnsEmptyList() throws Exception {
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of());

        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();

        assertEquals(List.of(), results);
        verifyNoInteractions(subprocessService);
    }

    @Test
    public void testFindPostInstallScripts_postInstallDisabled_returnsEmptyList() throws Exception {
        when(configService.readAllowPostInstallScripts()).thenReturn(false);

        List<Path> results = postInstallService.findPostInstallScripts();

        assertEquals(List.of(), results);
        verifyNoInteractions(fileService);
        verifyNoInteractions(subprocessService);
    }

    @Test
    public void testFindPostInstallScripts_sortsScripts() throws Exception {
        Path scriptB = Path.of(REPO_PATH, "post-install/02.sh");
        Path scriptA = Path.of(REPO_PATH, "post-install/01.sh");
        Path scriptC = Path.of(REPO_PATH, "post-install/folder/01.sh");

        List<Path> scriptPaths = List.of(scriptC, scriptB, scriptA);

        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(scriptPaths);

        List<Path> results = postInstallService.findPostInstallScripts();

        assertEquals(List.of(scriptA, scriptB, scriptC), results);
        verifyNoInteractions(subprocessService);
    }

    @Test
    public void testFindPostInstallScripts_globThrowsIOException_propagates() throws Exception {
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        doThrow(new IOException("base directory does not exist"))
            .when(fileService).glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN));

        assertThrows(IOException.class, postInstallService::findPostInstallScripts);

        verifyNoInteractions(subprocessService);
    }

    @Test
    public void testRunPostInstallScripts_resolvesHomeDirectoryPlaceholder() throws Exception {
        Path scriptPath = Path.of(REPO_PATH, "post-install/01.sh");

        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn("{HOME}/dotfiles");
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN))).thenReturn(List.of(scriptPath));
        when(subprocessService.executeCommand(any(), any())).thenReturn(new SubprocessResult(0, ""));

        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();

        assertEquals(List.of(new PostInstallScriptResult(true, "", scriptPath)), results);
        verify(fileService).glob(eq(Path.of(REPO_PATH)), eq(GLOB_PATTERN));
    }

    @Test
    public void testFindPostInstallScripts_windows_usesWin32GlobPattern() throws Exception {
        postInstallService = new PostInstallServiceImpl(GLOB_PATTERN, GLOB_PATTERN, WIN32_GLOB_PATTERN, "Windows 11", configService, fileService, subprocessService);
        Path scriptPath = Path.of(REPO_PATH, "post-install/01.ps1");
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(WIN32_GLOB_PATTERN))).thenReturn(List.of(scriptPath));

        List<Path> results = postInstallService.findPostInstallScripts();

        assertEquals(List.of(scriptPath), results);
    }

    @Test
    public void testRunPostInstallScripts_windows_runsScriptsWithPwsh() throws Exception {
        postInstallService = new PostInstallServiceImpl(GLOB_PATTERN, GLOB_PATTERN, WIN32_GLOB_PATTERN, "Windows 11", configService, fileService, subprocessService);
        Path scriptPath = Path.of(REPO_PATH, "post-install/01.ps1");
        when(configService.readAllowPostInstallScripts()).thenReturn(true);
        when(configService.readDotfileRepoPath()).thenReturn(REPO_PATH);
        when(fileService.glob(eq(Path.of(REPO_PATH)), eq(WIN32_GLOB_PATTERN))).thenReturn(List.of(scriptPath));
        when(subprocessService.executeCommand(any(), any())).thenReturn(new SubprocessResult(0, ""));

        List<PostInstallScriptResult> results = postInstallService.runPostInstallScripts();

        assertEquals(List.of(new PostInstallScriptResult(true, "", scriptPath)), results);
        verify(subprocessService).executeCommand(
            eq(scriptPath.getParent()),
            eq(List.of("pwsh", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", scriptPath.toString()))
        );
    }

    @Test
    public void testFindPostInstallScripts_windows_postInstallDisabled_returnsEmptyList() throws Exception {
        postInstallService = new PostInstallServiceImpl(GLOB_PATTERN, GLOB_PATTERN, WIN32_GLOB_PATTERN, "Windows 11", configService, fileService, subprocessService);
        when(configService.readAllowPostInstallScripts()).thenReturn(false);

        List<Path> results = postInstallService.findPostInstallScripts();

        assertEquals(List.of(), results);
        verifyNoInteractions(fileService);
        verifyNoInteractions(subprocessService);
    }
}
