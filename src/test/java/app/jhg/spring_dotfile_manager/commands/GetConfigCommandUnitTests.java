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
    public void testCall_success_callsPrintConfigAndReturnsZero() throws Exception {
        int result = command.call();

        assertEquals(0, result);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_printConfig_noSuchFileException_propagates() throws Exception {
        doThrow(new NoSuchFileException("config.yaml"))
            .when(configService).printConfig();

        assertThrows(NoSuchFileException.class, command::call);
    }

    @Test
    public void testCall_printConfig_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).printConfig();

        assertThrows(IOException.class, command::call);
    }
}
