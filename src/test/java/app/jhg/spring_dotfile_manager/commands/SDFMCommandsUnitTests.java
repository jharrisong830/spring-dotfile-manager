package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.shell.core.command.CommandContext;

import java.nio.file.Path;
import java.util.List;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.ConfigService;
import app.jhg.spring_dotfile_manager.service.DotfileService;

@ExtendWith(MockitoExtension.class)
public class SDFMCommandsUnitTests {

    @Mock
    private ConfigService configService;

    @Mock
    private DotfileService dotfileService;

    @Mock
    private CommandContext context;

    @Mock
    private PrintWriter outputWriter;

    private SDFMCommands commands;

    private static final String DEFAULT_REPO_PATH = "~/dotfiles";

    @BeforeEach
    void setUp() {
        commands = new SDFMCommands(DEFAULT_REPO_PATH, configService, dotfileService);
    }

    @Test
    public void testInit_pathProvidedAsArgument() throws Exception {
        when(context.outputWriter()).thenReturn(outputWriter);
        doNothing().when(outputWriter).println(anyString());

        commands.init("~/my-dotfiles", context);

        verify(configService).initializeConfig("~/my-dotfiles");
    }

    @Test
    public void testInit_noPathProvided_userEntersNothing_usesDefault() throws Exception {
        when(context.outputWriter()).thenReturn(outputWriter);
        doNothing().when(outputWriter).println(anyString());

        commands.init("", context);

        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testInit_noPathProvided_userEntersWhitespace_usesDefault() throws Exception {
        when(context.outputWriter()).thenReturn(outputWriter);
        doNothing().when(outputWriter).println(anyString());

        commands.init("", context);

        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testInit_fileAlreadyExistsException_propagates() throws Exception {
        doThrow(new FileAlreadyExistsException("already exists"))
            .when(configService).initializeConfig(any());

        assertThrows(FileAlreadyExistsException.class, () -> commands.init("~/my-dotfiles", context));
    }

    @Test
    public void testInit_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).initializeConfig(any());

        assertThrows(IOException.class, () -> commands.init("~/my-dotfiles", context));
    }


    @Test
    public void testGetConfig_readsConfigAndPrints() throws Exception {
        when(configService.readConfig()).thenReturn("~/my-dotfiles");
        when(context.outputWriter()).thenReturn(outputWriter);
        doNothing().when(outputWriter).println(anyString());

        commands.getConfig(context);

        verify(configService).readConfig();
        verify(outputWriter).println(contains("Configuration at:"));
        verify(outputWriter).println(contains("Using dotfile repository path:"));
    }

    @Test
    public void testGetConfig_noFile() throws Exception {
        doThrow(new NoSuchFileException("config file not found"))
            .when(configService).readConfig();

        assertThrows(NoSuchFileException.class, () -> commands.getConfig(context));
    }

    
    @Test
    public void testSetConfig_writesConfigAndPrints() throws Exception {
        when(context.outputWriter()).thenReturn(outputWriter);
        doNothing().when(outputWriter).println(anyString());

        commands.setConfig("~/new-dotfiles", context);

        verify(configService).updateConfig("~/new-dotfiles");
        verify(outputWriter).println(contains("Configuration at:"));
        verify(outputWriter).println(contains("Using dotfile repository path:"));
    }

    @Test
    public void testSetConfig_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).updateConfig(any());

        assertThrows(IOException.class, () -> commands.setConfig("~/new-dotfiles", context));
    }

    @Test
    public void testSetConfig_illegalArgumentException_propagates() throws Exception {
        doThrow(new IllegalArgumentException("invalid path"))
            .when(configService).updateConfig(any());

        assertThrows(IllegalArgumentException.class, () -> commands.setConfig("~/new-dotfiles", context));
    }


    @Test
    public void testList_noMarkersFound_printsEmptyMessage() throws Exception {
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(List.of());
        when(context.outputWriter()).thenReturn(outputWriter);

        commands.list(context);

        verify(outputWriter).println(contains("No dotfiles found"));
        verify(outputWriter, never()).println(contains("- "));
    }

    @Test
    public void testList_markersFound_printsEachMarker() throws Exception {
        List<DotfileMarkerModel> models = DotfileMarkerModel.fromMarkerFileContents(
            Path.of("/repo/zshrc.dotfile"),
            "name: .zshrc\nlocation: /home/user/.zshrc\n---\nname: .bashrc\nlocation: /home/user/.bashrc\n"
        );
        when(dotfileService.getAllDotfileMarkerModels()).thenReturn(models);
        when(context.outputWriter()).thenReturn(outputWriter);

        commands.list(context);

        verify(outputWriter).println(contains("Dotfiles in configured repository:"));
        verify(outputWriter, times(2)).println(contains("- "));
    }

    @Test
    public void testList_ioException_propagates() throws Exception {
        doThrow(new IOException("repo not found"))
            .when(dotfileService).getAllDotfileMarkerModels();

        assertThrows(IOException.class, () -> commands.list(context));
    }
}
