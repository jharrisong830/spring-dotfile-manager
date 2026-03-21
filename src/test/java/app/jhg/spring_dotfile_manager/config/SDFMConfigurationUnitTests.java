package app.jhg.spring_dotfile_manager.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import picocli.CommandLine.IExecutionExceptionHandler;

public class SDFMConfigurationUnitTests {

    private IExecutionExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SDFMConfiguration().executionExceptionHandler();
    }

    @Test
    public void testHandler_fileAlreadyExistsException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new FileAlreadyExistsException("config.yaml"), null, null));
    }

    @Test
    public void testHandler_noSuchFileException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new NoSuchFileException("config.yaml"), null, null));
    }

    @Test
    public void testHandler_fileNotFoundException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new FileNotFoundException("config.yaml"), null, null));
    }

    @Test
    public void testHandler_ioException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new IOException("disk full"), null, null));
    }

    @Test
    public void testHandler_illegalArgumentException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new IllegalArgumentException("invalid path"), null, null));
    }

    @Test
    public void testHandler_unknownException_returnsOne() throws Exception {
        assertEquals(1, handler.handleExecutionException(new RuntimeException("unexpected"), null, null));
    }
}
