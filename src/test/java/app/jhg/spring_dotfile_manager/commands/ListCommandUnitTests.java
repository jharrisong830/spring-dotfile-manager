package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
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
public class ListCommandUnitTests {

    @Mock
    private DotfileService dotfileService;

    private ListCommand command;

    @BeforeEach
    void setUp() {
        command = new ListCommand(dotfileService);
    }

    @Test
    public void testCall_noMarkersFound_returnsZero() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());

        int result = command.call();

        assertEquals(0, result);
    }

    @Test
    public void testCall_markersFound_returnsZero() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);

        int result = command.call();

        assertEquals(0, result);
    }

    @Test
    public void testCall_ioException_propagates() throws Exception {
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getAllDotfileMarkerModels();

        assertThrows(IOException.class, command::call);
    }
}
