package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
public class SetConfigCommandUnitTests {

    @Mock
    private ConfigService configService;

    private SetConfigCommand commandWithStdin(String stdinInput) {
        return new SetConfigCommand(configService, new BufferedReader(new StringReader(stdinInput)));
    }

    private void parseArgs(SetConfigCommand cmd, String... args) {
        new CommandLine(cmd).parseArgs(args);
    }

    @Test
    public void testCall_pathProvidedAsArgument_callsUpdateConfig() throws Exception {
        SetConfigCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/new-dotfiles");

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).updateConfig("~/new-dotfiles");
        verify(configService).printConfig();
    }

    @Test
    public void testCall_pathArgumentIsWhitespace_stdinHasPath_usesStdinPath() throws Exception {
        SetConfigCommand cmd = commandWithStdin("~/from-stdin");
        parseArgs(cmd, "   ");

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).updateConfig("~/from-stdin");
    }

    @Test
    public void testCall_noPathProvided_stdinHasCustomPath_usesStdinPath() throws Exception {
        SetConfigCommand cmd = commandWithStdin("~/new-dotfiles");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).updateConfig("~/new-dotfiles");
    }

    @Test
    public void testCall_noPathProvided_stdinEmpty_throwsIllegalArgumentException() throws Exception {
        SetConfigCommand cmd = commandWithStdin("");
        parseArgs(cmd);

        assertThrows(IllegalArgumentException.class, cmd::call);
        verify(configService, never()).updateConfig(any());
    }

    @Test
    public void testCall_noPathProvided_stdinWhitespace_throwsIllegalArgumentException() throws Exception {
        SetConfigCommand cmd = commandWithStdin("   ");
        parseArgs(cmd);

        assertThrows(IllegalArgumentException.class, cmd::call);
        verify(configService, never()).updateConfig(any());
    }

    @Test
    public void testCall_noPathProvided_stdinReturnsNull_throwsIllegalArgumentException() throws Exception {
        BufferedReader nullReader = mock(BufferedReader.class);
        when(nullReader.readLine()).thenReturn(null);
        SetConfigCommand cmd = new SetConfigCommand(configService, nullReader);
        parseArgs(cmd);

        assertThrows(IllegalArgumentException.class, cmd::call);
        verify(configService, never()).updateConfig(any());
    }

    @Test
    public void testCall_updateConfig_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).updateConfig(any());
        SetConfigCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/new-dotfiles");

        assertThrows(IOException.class, cmd::call);
    }

    @Test
    public void testCall_updateConfig_illegalArgumentException_propagates() throws Exception {
        doThrow(new IllegalArgumentException("invalid path"))
            .when(configService).updateConfig(any());
        SetConfigCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/new-dotfiles");

        assertThrows(IllegalArgumentException.class, cmd::call);
    }

    @Test
    public void testCall_printConfig_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).printConfig();
        SetConfigCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/new-dotfiles");

        assertThrows(IOException.class, cmd::call);
        verify(configService).updateConfig("~/new-dotfiles");
    }
}
