package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class ListCommandUnitTests {

    @Mock
    private DotfileService dotfileService;

    private ListCommand command;

    private Logger commandLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        command = new ListCommand(dotfileService);

        commandLogger = (Logger) LoggerFactory.getLogger(ListCommand.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        commandLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        commandLogger.detachAppender(logAppender);
    }

    private void parseArgs(ListCommand cmd, String... args) {
        new CommandLine(cmd).parseArgs(args);
    }

    private List<String> warningMessages() {
        return logAppender.list.stream()
            .filter(event -> event.getLevel() == Level.WARN)
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
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
        
        // at least once to decide if it should be included, then used for pretty print if included
        verify(dotfileService, atLeastOnce()).getTargetPathForCurrentSystem(markers.get(0));
        verify(dotfileService, atLeastOnce()).getTargetPathForCurrentSystem(markers.get(1));
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

        // at least once to decide if it should be included, then used for pretty print if included
        verify(dotfileService, atLeastOnce()).getTargetPathForCurrentSystem(markers.get(0));
        verify(dotfileService, atLeastOnce()).getTargetPathForCurrentSystem(markers.get(1));
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

    @Test
    public void testCall_defaultMode_duplicateKeys_logsWarning() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: bin\nlocation: /home/user/bin\nkey: bin\n---\nname: bin\nlocation: /home/user/other-bin\nkey: bin\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(dotfileService.getTargetPathForCurrentSystem(any())).thenReturn(Path.of("/home/user/bin"));

        int result = command.call();

        assertEquals(0, result);
        List<String> warnings = warningMessages();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("'bin'"));
    }

    @Test
    public void testCall_defaultMode_noDuplicateKeys_doesNotLogWarning() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(dotfileService.getTargetPathForCurrentSystem(any())).thenReturn(Path.of("/home/user/.zshrc"));

        int result = command.call();

        assertEquals(0, result);
        assertTrue(warningMessages().isEmpty());
    }

    @Test
    public void testCall_defaultMode_duplicateKeyMarkerFilteredByPlatform_notFlagged() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: bin\nlocation: /home/user/bin\nkey: bin\n---\nname: bin\nlocation: /home/user/other-bin\nkey: bin\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(0))).thenReturn(Path.of("/home/user/bin"));
        when(dotfileService.getTargetPathForCurrentSystem(markers.get(1))).thenReturn(null);

        int result = command.call();

        assertEquals(0, result);
        assertTrue(warningMessages().isEmpty());
    }

    @Test
    public void testCall_includeAll_duplicateKeys_doesNotLogWarning() throws Exception {
        List<DotfileMarkerModel> markers = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/shell.dotfile"),
            "name: bin\nlocation: /home/user/bin\nkey: bin\n---\nname: bin\nlocation: /home/user/other-bin\nkey: bin\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(markers);
        parseArgs(command, "--all");

        int result = command.call();

        assertEquals(0, result);
        assertTrue(warningMessages().isEmpty());
        verify(dotfileService, never()).getTargetPathForCurrentSystem(any());
    }
}
