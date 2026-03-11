package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;

public class SDFMConfigurationUnitTests {

    private ExitStatusExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SDFMConfiguration().exitStatusExceptionMapper();
    }

    @Test
    public void testExitStatusExceptionMapper_fileExistsException() {
        ExitStatus result = mapper.apply(new FileExistsException("config.yaml"));

        assertEquals(1, result.code());
        assertTrue(result.description().contains("config.yaml"));
    }

    @Test
    public void testExitStatusExceptionMapper_ioException() {
        ExitStatus result = mapper.apply(new IOException("disk full"));

        assertEquals(1, result.code());
        assertTrue(result.description().contains("disk full"));
    }

    @Test
    public void testExitStatusExceptionMapper_unknownException() {
        ExitStatus result = mapper.apply(new RuntimeException("something unexpected"));

        assertEquals(1, result.code());
        assertTrue(result.description().contains("something unexpected"));
    }
}
