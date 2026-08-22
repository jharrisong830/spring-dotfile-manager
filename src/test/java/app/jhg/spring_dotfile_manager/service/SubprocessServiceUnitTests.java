// spring-dotfile-manager
// Copyright (C) 2026  John Graham

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

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
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import app.jhg.spring_dotfile_manager.util.Os;

@ExtendWith(MockitoExtension.class)
public class SubprocessServiceUnitTests {

    @Mock
    private FileService fileService;

    private SubprocessService subprocessService;

    private static final long TIMEOUT = 1000;
    private static final Path CWD = Path.of(".");

    // executeCommand runs the given args as a standalone process, with no shell wrapping - "echo"/"false"/"sleep"
    // aren't standalone executables on Windows (echo/exit are cmd.exe builtins, there's no sleep.exe), so these
    // tests route through cmd.exe/ping there instead
    private static final boolean IS_WINDOWS =
        FormattingUtils.getResolvedOsName(System.getProperty("os.name")) == Os.WIN32;

    @BeforeEach
    void setUp() {
        subprocessService = new SubprocessServiceImpl(TIMEOUT, fileService);
    }

    @Test
    public void testExecuteCommand_success() throws Exception {
        List<String> args = IS_WINDOWS ? List.of("cmd", "/c", "echo", "hi") : List.of("echo", "hi");
        SubprocessResult expectedResult = new SubprocessResult(0, IS_WINDOWS ? "hi\r\n" : "hi\n");

        when(fileService.isDirectory(CWD)).thenReturn(true);

        SubprocessResult res = subprocessService.executeCommand(CWD, args);
        assertEquals(expectedResult, res);
    }

    @Test
    public void testExecuteCommand_nonZeroExitCode() throws Exception {
        List<String> args = IS_WINDOWS ? List.of("cmd", "/c", "exit", "1") : List.of("false");
        SubprocessResult expectedResult = new SubprocessResult(1, "");

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
        // sleeps past the 1 second timeout; there's no standalone sleep.exe on Windows, so ping against
        // localhost is used as a delay instead (~1s per echo request)
        List<String> args = IS_WINDOWS ? List.of("ping", "-n", "3", "127.0.0.1") : List.of("sleep", "2");

        when(fileService.isDirectory(CWD)).thenReturn(true);

        assertThrows(TimeoutException.class, () -> subprocessService.executeCommand(CWD, args));
    }

    @Test
    public void testExecuteCommand_interruptedThrows() throws Exception {
        List<String> args = IS_WINDOWS ? List.of("ping", "-n", "11", "127.0.0.1") : List.of("sleep", "10");
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
