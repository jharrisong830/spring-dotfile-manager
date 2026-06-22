package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.jhg.spring_dotfile_manager.model.SubprocessResult;

@ExtendWith(MockitoExtension.class)
public class SubprocessServiceUnitTests {
    
    @Mock
    private FileService fileService;

    private SubprocessService subprocessService;

    private static final long TIMEOUT = 1000;
    private static final Path CWD = Path.of(".");

    @BeforeEach
    void setUp() {
        subprocessService = new SubprocessServiceImpl(TIMEOUT, fileService);
    }

    @Test
    public void testExecuteCommand_success() throws Exception {
        SubprocessResult expectedResult = new SubprocessResult(0, "hi\n");
        List<String> args = List.of("echo", "hi");

        when(fileService.isDirectory(CWD)).thenReturn(true);

        SubprocessResult res = subprocessService.executeCommand(CWD, args);
        assertEquals(expectedResult, res);
    }

    @Test
    public void testExecuteCommand_nonZeroExitCode() throws Exception {
        SubprocessResult expectedResult = new SubprocessResult(1, "");
        List<String> args = List.of("false");

        when(fileService.isDirectory(CWD)).thenReturn(true);

        SubprocessResult res = subprocessService.executeCommand(CWD, args);
        assertEquals(expectedResult, res);
    }

    @Test
    public void testExecuteCommand_emptyCommandThrows() throws Exception {
        List<String> args = List.of();

        assertThrows(IllegalArgumentException.class, () -> subprocessService.executeCommand(CWD, args));
        verifyNoInteractions(fileService);
    }

    @Test
    public void testExecuteCommand_notDirectoryThrows() throws Exception {
        List<String> args = List.of("echo", "hi");

        when(fileService.isDirectory(CWD)).thenReturn(false);

        assertThrows(IOException.class, () -> subprocessService.executeCommand(CWD, args));
    }

    @Test
    public void testExecuteCommand_timeoutThrows() throws Exception {
        List<String> args = List.of("sleep", "2"); // sleeps past the 1 second timeout

        when(fileService.isDirectory(CWD)).thenReturn(true);

        assertThrows(TimeoutException.class, () -> subprocessService.executeCommand(CWD, args));
    }

    @Test
    public void testExecuteCommand_interruptedThrows() throws Exception {
        List<String> args = List.of("sleep", "10");
        when(fileService.isDirectory(CWD)).thenReturn(true);
        subprocessService = new SubprocessServiceImpl(10000, fileService); // longer timeout

        AtomicReference<Throwable> caught = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                subprocessService.executeCommand(CWD, args);
            } catch (Throwable t) {
                caught.set(t);
            }
        });

        worker.start();
        Thread.sleep(1000); // reach `waitFor`
        worker.interrupt();
        worker.join(2000);

        assertFalse(worker.isAlive());
        assertInstanceOf(InterruptedException.class, caught.get());
    }
}
