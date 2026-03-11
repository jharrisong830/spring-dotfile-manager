package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.shell.core.InputReader;
import org.springframework.shell.core.command.CommandContext;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;
import app.jhg.spring_dotfile_manager.service.ConfigService;

@ExtendWith(MockitoExtension.class)
public class SDFMCommandsUnitTests {

    @Mock
    private ConfigService configService;

    @Mock
    private CommandContext context;

    @Mock
    private InputReader inputReader;

    private SDFMCommands commands;

    private static final String DEFAULT_REPO_PATH = "~/dotfiles";

    @BeforeEach
    void setUp() {
        commands = new SDFMCommands(DEFAULT_REPO_PATH, configService);
    }

    @Test
    public void testInit_pathProvidedAsArgument() throws Exception {
        commands.init("~/my-dotfiles", context);

        verify(configService).initializeConfig("~/my-dotfiles");
        verifyNoInteractions(context);
    }

    @Test
    public void testInit_noPathProvided_userEntersPath() throws Exception {
        when(context.inputReader()).thenReturn(inputReader);
        when(inputReader.readInput(any())).thenReturn("~/entered-dotfiles");

        commands.init("", context);

        verify(configService).initializeConfig("~/entered-dotfiles");
    }

    @Test
    public void testInit_noPathProvided_userEntersNothing_usesDefault() throws Exception {
        when(context.inputReader()).thenReturn(inputReader);
        when(inputReader.readInput(any())).thenReturn("");

        commands.init("", context);

        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testInit_fileExistsException_propagates() throws Exception {
        doThrow(new FileExistsException("already exists"))
            .when(configService).initializeConfig(any());

        assertThrows(FileExistsException.class, () -> commands.init("~/my-dotfiles", context));
    }

    @Test
    public void testInit_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).initializeConfig(any());

        assertThrows(IOException.class, () -> commands.init("~/my-dotfiles", context));
    }
}
