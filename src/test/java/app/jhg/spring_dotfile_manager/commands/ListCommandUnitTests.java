package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
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
public class ListCommandUnitTests {

    @Mock
    private DotfileService dotfileService;

    private ListCommand command;

    @BeforeEach
    void setUp() {
        command = new ListCommand(dotfileService);
    }

    private void parseArgs(ListCommand cmd, String... args) {
        new CommandLine(cmd).parseArgs(args);
    }

    @Test
    public void testCall_noMarkersFound_returnsOne() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());

        int result = command.call();

        assertEquals(1, result);
    }

    @Test
    public void testCall_defaultMode_markersFound_callsGetTargetPathForEachMarker() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(dotfileService.getTargetPathForCurrentSystem(any())).thenReturn(Path.of("/home/user/.zshrc"));

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).getTargetPathForCurrentSystem(markers.get(0));
        verify(dotfileService).getTargetPathForCurrentSystem(markers.get(1));
    }

    @Test
    public void testCall_defaultMode_markerWithNullTargetPath_isFiltered() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(Path.of("/home/user/.zshrc"));
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(1))).thenReturn(null);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).getTargetPathForCurrentSystem(markers.get(0));
        verify(dotfileService).getTargetPathForCurrentSystem(markers.get(1));
    }

    @Test
    public void testCall_includeAll_markersFound_doesNotCallGetTargetPath() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        parseArgs(command, "--all");

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, never()).getTargetPathForCurrentSystem(any());
    }

    @Test
    public void testCall_defaultMode_allMarkersFiltered_returnsOne() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(dotfileService.getTargetPathForCurrentSystem(any())).thenReturn(null);

        int result = command.call();

        assertEquals(1, result);
    }

    @Test
    public void testCall_ioException_propagates() throws Exception {
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getAllDotfileMarkerModels();

        assertThrows(IOException.class, command::call);
    }
}
