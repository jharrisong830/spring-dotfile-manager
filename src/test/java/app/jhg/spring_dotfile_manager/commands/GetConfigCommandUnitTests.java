package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.service.ConfigService;

@ExtendWith(MockitoExtension.class)
public class GetConfigCommandUnitTests {

    @Mock
    private ConfigService configService;

    private GetConfigCommand command;

    @BeforeEach
    void setUp() {
        command = new GetConfigCommand(configService);
    }

    @Test
    public void testCall_success_returnsZero() throws Exception {
        when(configService.readConfig()).thenReturn("~/dotfiles");

        int result = command.call();

        assertEquals(0, result);
        verify(configService).readConfig();
    }

    @Test
    public void testCall_noSuchFileException_propagates() throws Exception {
        doThrow(new NoSuchFileException("config.yaml"))
            .when(configService).readConfig();

        assertThrows(NoSuchFileException.class, command::call);
    }

    @Test
    public void testCall_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).readConfig();

        assertThrows(IOException.class, command::call);
    }
}
