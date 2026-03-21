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

@ExtendWith(MockitoExtension.class)
public class RelinkCommandUnitTests {

    @Mock
    private DotfileService dotfileService;

    private RelinkCommand command;

    @BeforeEach
    void setUp() {
        command = new RelinkCommand(dotfileService);
    }

    @Test
    public void testCall_noMarkersFound_skipsRelink() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService, never()).relinkDotfile(any());
    }

    @Test
    public void testCall_markersFound_relinksEachMarker() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);

        int result = command.call();

        assertEquals(0, result);
        verify(dotfileService).relinkDotfile(markers.get(0));
        verify(dotfileService).relinkDotfile(markers.get(1));
    }

    @Test
    public void testCall_getAllDotfileMarkerModels_ioException_propagates() throws Exception {
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getAllDotfileMarkerModels();

        assertThrows(IOException.class, command::call);
        verify(dotfileService, never()).relinkDotfile(any());
    }

    @Test
    public void testCall_relinkDotfile_ioException_propagates() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        doThrow(new IOException("file exists"))
            .when(dotfileService).relinkDotfile(any());

        assertThrows(IOException.class, command::call);
    }
}
