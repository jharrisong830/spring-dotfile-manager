package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import app.jhg.spring_dotfile_manager.service.PostInstallService;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class RelinkCommandUnitTests {

    @Mock
    private DotfileService dotfileService;

    @Mock
    private PostInstallService postInstallService;

    @Mock
    private BufferedReader stdinReader;

    private RelinkCommand command;

    @BeforeEach
    void setUp() {
        command = new RelinkCommand(dotfileService, postInstallService, stdinReader);
    }

    @Test
    public void testCall_noMarkersFound_skipsRelink() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, never()).relinkDotfile(any());
        verify(dotfileService, never()).overwriteExistingDotfile(any());
    }

    @Test
    public void testCall_markersFound_relinksEachMarker() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).relinkDotfile(markers.get(0));
        verify(dotfileService).relinkDotfile(markers.get(1));
        verify(dotfileService, never()).overwriteExistingDotfile(any());
    }

    @Test
    public void testCall_getAllDotfileMarkerModels_ioException_propagates() throws Exception {
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getAllDotfileMarkerModels();

        assertThrows(IOException.class, command::call);
        verify(dotfileService, never()).relinkDotfile(any());
        verify(dotfileService, never()).overwriteExistingDotfile(any());
    }

    @Test
    public void testCall_relinkDotfile_ioException_propagates() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new IOException("unexpected i/o error"))
            .when(dotfileService).relinkDotfile(any());

        assertThrows(IOException.class, command::call);
        verify(dotfileService, never()).overwriteExistingDotfile(any());
    }

    @Test
    public void testCall_fileAlreadyExists_userConfirmsYes_overwrites() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        Path targetPath = Path.of("/home/user/.zshrc");
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).relinkDotfile(any());
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(targetPath);
        when(stdinReader.readLine()).thenReturn("yes");
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).overwriteExistingDotfile(markers.get(0));
    }

    @Test
    public void testCall_fileAlreadyExists_userConfirmsYes_caseInsensitive_overwrites() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        Path targetPath = Path.of("/home/user/.zshrc");
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).relinkDotfile(any());
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(targetPath);
        when(stdinReader.readLine()).thenReturn("YES");
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).overwriteExistingDotfile(markers.get(0));
    }

    @Test
    public void testCall_fileAlreadyExists_userDeclines_skips() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        Path targetPath = Path.of("/home/user/.zshrc");
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).relinkDotfile(any());
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(targetPath);
        when(stdinReader.readLine()).thenReturn("no");
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, never()).overwriteExistingDotfile(any());
    }

    @Test
    public void testCall_fileAlreadyExists_nullResponse_skips() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        Path targetPath = Path.of("/home/user/.zshrc");
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).relinkDotfile(any());
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(targetPath);
        when(stdinReader.readLine()).thenReturn(null);
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, never()).overwriteExistingDotfile(any());
    }

    @Test
    public void testCall_fileAlreadyExists_userConfirmsYes_overwriteFails_doesNotPropagate() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        Path targetPath = Path.of("/home/user/.zshrc");
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).relinkDotfile(any());
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(targetPath);
        when(stdinReader.readLine()).thenReturn("yes");
        doThrow(new IOException("permission denied"))
            .when(dotfileService).overwriteExistingDotfile(any());
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(1, result); // does not propogate, but returns non-zero exit code
    }

    @Test
    public void testCall_fileAlreadyExists_userConfirmsYes_overwriteFailsOnFirst_continuesLoop() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).relinkDotfile(markers.get(0));
        doThrow(new FileAlreadyExistsException("/home/user/.bashrc"))
            .when(dotfileService).relinkDotfile(markers.get(1));
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(Path.of("/home/user/.zshrc"));
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(1))).thenReturn(Path.of("/home/user/.bashrc"));
        when(stdinReader.readLine()).thenReturn("yes");
        doThrow(new IOException("permission denied"))
            .when(dotfileService).overwriteExistingDotfile(markers.get(0));
        when(postInstallService.findPostInstallScripts()).thenReturn(List.of());

        int result = command.call();

        assertEquals(1, result); // does not propogate, but returns non-zero exit code
        verify(dotfileService).overwriteExistingDotfile(markers.get(0));
        verify(dotfileService).overwriteExistingDotfile(markers.get(1));
    }

    @Test
    public void testCall_postInstallScripts_userConfirmsYes_allSucceed_returnsZero() throws Exception {
        List<Path> scripts = List.of(Path.of("/repo/post-install/01.sh"), Path.of("/repo/post-install/02.sh"));
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());
        when(postInstallService.findPostInstallScripts()).thenReturn(scripts);
        when(stdinReader.readLine()).thenReturn("yes");
        when(postInstallService.runPostInstallScripts()).thenReturn(List.of(
            new PostInstallScriptResult(true, "done", scripts.get(0)),
            new PostInstallScriptResult(true, "done", scripts.get(1))
        ));

        int result = command.call();

        assertEquals(0, result);
    }

    @Test
    public void testCall_postInstallScripts_userConfirmsYes_oneFails_returnsOne() throws Exception {
        List<Path> scripts = List.of(Path.of("/repo/post-install/01.sh"), Path.of("/repo/post-install/02.sh"));
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());
        when(postInstallService.findPostInstallScripts()).thenReturn(scripts);
        when(stdinReader.readLine()).thenReturn("yes");
        when(postInstallService.runPostInstallScripts()).thenReturn(List.of(
            new PostInstallScriptResult(false, "command error", scripts.get(0)),
            new PostInstallScriptResult(true, "done", scripts.get(1))
        ));

        int result = command.call();

        assertEquals(1, result);
    }

    @Test
    public void testCall_postInstallScripts_userDeclines_doesNotRun() throws Exception {
        List<Path> scripts = List.of(Path.of("/repo/post-install/01.sh"));
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());
        when(postInstallService.findPostInstallScripts()).thenReturn(scripts);
        when(stdinReader.readLine()).thenReturn("no");

        int result = command.call();

        assertEquals(0, result);
        verify(postInstallService, never()).runPostInstallScripts();
    }

    @Test
    public void testCall_postInstallScripts_findThrowsIOException_returnsOne_doesNotPropagate() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());
        doThrow(new IOException("could not read config"))
            .when(postInstallService).findPostInstallScripts();

        int result = command.call();

        assertEquals(1, result);
        verify(postInstallService, never()).runPostInstallScripts();
    }

    @Test
    public void testParseArgs_keyOptionAccepted() {
        // proves the KeyMixin is wired into RelinkCommand; the mixin's own parsing
        // behavior is covered by KeyMixinUnitTests
        assertDoesNotThrow(() -> new CommandLine(command).parseArgs("--key", "bin"));
    }
}
