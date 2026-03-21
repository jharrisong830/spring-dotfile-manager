package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class InitCommandUnitTests {

    @Mock
    private ConfigService configService;

    private static final String DEFAULT_REPO_PATH = "~/dotfiles";

    private InitCommand commandWithStdin(String stdinInput) {
        return new InitCommand(DEFAULT_REPO_PATH, configService, new BufferedReader(new StringReader(stdinInput)));
    }

    // Parses picocli @Parameters fields without going through CommandLine.execute(),
    // so exceptions propagate rather than being swallowed by the exception handler.
    private void parseArgs(InitCommand cmd, String... args) {
        new CommandLine(cmd).parseArgs(args);
    }

    @Test
    public void testCall_pathProvidedAsArgument_callsInitializeConfigWithPath() throws Exception {
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles");

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig("~/my-dotfiles");
    }

    @Test
    public void testCall_pathArgumentIsWhitespace_readsStdin_usesDefault() throws Exception {
        // Whitespace-only arg trims to "", which triggers stdin prompt
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "   ");

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testCall_noPathProvided_stdinEmpty_usesDefault() throws Exception {
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testCall_noPathProvided_stdinWhitespace_usesDefault() throws Exception {
        InitCommand cmd = commandWithStdin("   ");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testCall_noPathProvided_stdinHasCustomPath_usesCustomPath() throws Exception {
        InitCommand cmd = commandWithStdin("~/custom-dotfiles");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig("~/custom-dotfiles");
    }

    @Test
    public void testCall_noPathProvided_stdinReturnsNull_usesDefault() throws Exception {
        // readLine() returns null at EOF — the null check in call() falls through to default
        BufferedReader nullReader = mock(BufferedReader.class);
        when(nullReader.readLine()).thenReturn(null);
        InitCommand cmd = new InitCommand(DEFAULT_REPO_PATH, configService, nullReader);
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH);
    }

    @Test
    public void testCall_fileAlreadyExistsException_propagates() throws Exception {
        doThrow(new FileAlreadyExistsException("already exists"))
            .when(configService).initializeConfig(any());
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles");

        assertThrows(FileAlreadyExistsException.class, cmd::call);
    }

    @Test
    public void testCall_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).initializeConfig(any());
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles");

        assertThrows(IOException.class, cmd::call);
    }
}
