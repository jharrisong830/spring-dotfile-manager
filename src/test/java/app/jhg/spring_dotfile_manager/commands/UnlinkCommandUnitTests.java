package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import app.jhg.spring_dotfile_manager.service.DotfileService;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class UnlinkCommandUnitTests {

    @Mock
    private DotfileService dotfileService;

    private UnlinkCommand command;

    @BeforeEach
    void setUp() {
        command = new UnlinkCommand(dotfileService);
        // populate the @Mixin field with defaults, as picocli would when parsing zero args
        new CommandLine(command).parseArgs();
    }

    @Test
    public void testCall_noMarkersFound_skipsUnlink() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, never()).unlinkDotfile(any());
    }

    @Test
    public void testCall_singleMarker_unlinksIt() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).unlinkDotfile(markers.get(0));
    }

    @Test
    public void testCall_multipleMarkers_unlinksEach() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).unlinkDotfile(markers.get(0));
        verify(dotfileService).unlinkDotfile(markers.get(1));
    }

    @Test
    public void testCall_getAllDotfileMarkerModels_ioException_propagates() throws Exception {
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getAllDotfileMarkerModels();

        assertThrows(IOException.class, command::call);
        verify(dotfileService, never()).unlinkDotfile(any());
    }

    @Test
    public void testCall_unlinkDotfile_ioException_doesNotPropagate() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new IOException("unexpected i/o error"))
            .when(dotfileService).unlinkDotfile(any());

        int result = command.call();

        assertEquals(1, result); // does not propogate, but returns non-zero exit code
    }

    @Test
    public void testCall_firstMarkerFails_continuesWithSecond() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new FileAlreadyExistsException("/home/user/.zshrc"))
            .when(dotfileService).unlinkDotfile(markers.get(0));

        int result = command.call();

        assertEquals(1, result); // does not propogate, but returns non-zero exit code
        verify(dotfileService).unlinkDotfile(markers.get(0));
        verify(dotfileService).unlinkDotfile(markers.get(1));
    }

    @Test
    public void testParseArgs_keyOptionAccepted() {
        // proves the KeyMixin is wired into UnlinkCommand; the mixin's own parsing
        // behavior is covered by KeyMixinUnitTests
        assertDoesNotThrow(() -> new CommandLine(command).parseArgs("--key", "bin"));
    }

    private void withKey(String key) {
        new CommandLine(command).parseArgs("--key", key);
    }

    @Test
    public void testCall_keyOption_noMatch_returnsOneWithoutUnlinking() throws Exception {
        withKey("bin");
        when(dotfileService.getMarkersByKeyForCurrentSystem("bin")).thenReturn(List.of());

        int result = command.call();

        assertEquals(1, result);
        verify(dotfileService, never()).unlinkDotfile(any());
        verify(dotfileService, never()).getAllDotfileMarkerModels();
    }

    @Test
    public void testCall_keyOption_ambiguousMatch_returnsOneWithoutUnlinking() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: bin\nlocation: /home/user/bin\nkey: bin\n---\nname: bin\nlocation: /home/user/other-bin\nkey: bin\n"
        );
        withKey("bin");
        when(dotfileService.getMarkersByKeyForCurrentSystem("bin")).thenReturn(markers);

        int result = command.call();

        assertEquals(1, result);
        verify(dotfileService, never()).unlinkDotfile(any());
    }

    @Test
    public void testCall_keyOption_singleMatch_unlinksOnlyThatMarker() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\nkey: shell-config\n"
        );
        withKey("shell-config");
        when(dotfileService.getMarkersByKeyForCurrentSystem("shell-config")).thenReturn(markers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).unlinkDotfile(markers.get(0));
        verify(dotfileService, never()).getAllDotfileMarkerModels();
    }

    @Test
    public void testCall_keyOption_getMarkersByKeyForCurrentSystemThrowsIOException_propagates() throws Exception {
        withKey("bin");
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getMarkersByKeyForCurrentSystem("bin");

        assertThrows(IOException.class, command::call);
        verify(dotfileService, never()).unlinkDotfile(any());
    }
}
