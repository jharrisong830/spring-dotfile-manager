// spring-dotfile-manager
// Copyright (C) 2026  John Graham

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

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

    private void withKeys(String... keys) {
        List<String> args = new java.util.ArrayList<>();
        for (String key : keys) {
            args.add("--key");
            args.add(key);
        }
        new CommandLine(command).parseArgs(args.toArray(new String[0]));
    }

    @Test
    public void testCall_keyOption_noMatch_returnsOneWithoutUnlinking() throws Exception {
        withKeys("bin");
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
        withKeys("bin");
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
        withKeys("shell-config");
        when(dotfileService.getMarkersByKeyForCurrentSystem("shell-config")).thenReturn(markers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).unlinkDotfile(markers.get(0));
        verify(dotfileService, never()).getAllDotfileMarkerModels();
    }

    @Test
    public void testCall_keyOption_getMarkersByKeyForCurrentSystemThrowsIOException_propagates() throws Exception {
        withKeys("bin");
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getMarkersByKeyForCurrentSystem("bin");

        assertThrows(IOException.class, command::call);
        verify(dotfileService, never()).unlinkDotfile(any());
    }

    @Test
    public void testCall_multipleKeyOptions_eachUnambiguous_unlinksAll() throws Exception {
        List<DotfileMarkerModel> zshrcMarkers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\nkey: zsh\n"
        );
        List<DotfileMarkerModel> vimrcMarkers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/vimrc.dotfile"),
            "name: .vimrc\nlocation: /home/user/.vimrc\nkey: vim\n"
        );
        withKeys("zsh", "vim");
        when(dotfileService.getMarkersByKeyForCurrentSystem("zsh")).thenReturn(zshrcMarkers);
        when(dotfileService.getMarkersByKeyForCurrentSystem("vim")).thenReturn(vimrcMarkers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).unlinkDotfile(zshrcMarkers.get(0));
        verify(dotfileService).unlinkDotfile(vimrcMarkers.get(0));
        verify(dotfileService, never()).getAllDotfileMarkerModels();
    }

    @Test
    public void testCall_multipleKeyOptions_oneAmbiguous_returnsOneWithoutUnlinkingAny() throws Exception {
        List<DotfileMarkerModel> zshrcMarkers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\nkey: zsh\n"
        );
        List<DotfileMarkerModel> binMarkers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/bin.dotfile"),
            "name: bin\nlocation: /home/user/bin\nkey: bin\n---\nname: bin\nlocation: /home/user/other-bin\nkey: bin\n"
        );
        withKeys("zsh", "bin");
        when(dotfileService.getMarkersByKeyForCurrentSystem("zsh")).thenReturn(zshrcMarkers);
        when(dotfileService.getMarkersByKeyForCurrentSystem("bin")).thenReturn(binMarkers);

        int result = command.call();

        assertEquals(1, result);
        verify(dotfileService, never()).unlinkDotfile(any());
    }

    @Test
    public void testCall_multipleKeyOptions_oneNoMatch_returnsOneWithoutUnlinkingAny() throws Exception {
        List<DotfileMarkerModel> zshrcMarkers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\nkey: zsh\n"
        );
        withKeys("zsh", "missing");
        when(dotfileService.getMarkersByKeyForCurrentSystem("zsh")).thenReturn(zshrcMarkers);
        when(dotfileService.getMarkersByKeyForCurrentSystem("missing")).thenReturn(List.of());

        int result = command.call();

        assertEquals(1, result);
        verify(dotfileService, never()).unlinkDotfile(any());
    }

    @Test
    public void testCall_sameKeyOptionRepeated_unlinksOnlyOnce() throws Exception {
        List<DotfileMarkerModel> zshrcMarkers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\nkey: zsh\n"
        );
        withKeys("zsh", "zsh");
        when(dotfileService.getMarkersByKeyForCurrentSystem("zsh")).thenReturn(zshrcMarkers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, times(1)).unlinkDotfile(zshrcMarkers.get(0));
        verify(dotfileService, times(1)).getMarkersByKeyForCurrentSystem("zsh");
    }
}
