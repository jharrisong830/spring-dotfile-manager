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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private static final boolean DEFUALT_ALLOW_POST_INSTALL = false;

    private InitCommand commandWithStdin(String stdinInput) {
        return new InitCommand(DEFAULT_REPO_PATH, DEFUALT_ALLOW_POST_INSTALL, configService, new BufferedReader(new StringReader(stdinInput)));
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
        verify(configService).initializeConfig("~/my-dotfiles", false);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_pathArgumentIsWhitespace_readsStdin_usesDefault() throws Exception {
        // Whitespace-only arg trims to "", which triggers stdin prompt
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "   ");

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH, false);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_noPathProvided_stdinEmpty_usesDefault() throws Exception {
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH, false);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_noPathProvided_stdinWhitespace_usesDefault() throws Exception {
        InitCommand cmd = commandWithStdin("   ");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH, false);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_noPathProvided_stdinHasCustomPath_usesCustomPath() throws Exception {
        InitCommand cmd = commandWithStdin("~/custom-dotfiles");
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig("~/custom-dotfiles", false);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_noPathProvided_stdinReturnsNull_usesDefault() throws Exception {
        // readLine() returns null at EOF — the null check in call() falls through to default
        BufferedReader nullReader = mock(BufferedReader.class);
        when(nullReader.readLine()).thenReturn(null);
        InitCommand cmd = new InitCommand(DEFAULT_REPO_PATH, DEFUALT_ALLOW_POST_INSTALL, configService, nullReader);
        parseArgs(cmd);

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig(DEFAULT_REPO_PATH, false);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_allowPostInstallProvided_true_passesTrueToInitializeConfig() throws Exception {
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles", "--allow-post-install-scripts=true");

        int result = cmd.call();

        assertEquals(0, result);
        verify(configService).initializeConfig("~/my-dotfiles", true);
        verify(configService).printConfig();
    }

    @Test
    public void testCall_allowPostInstallInvalidValue_throws() throws Exception {
        InitCommand cmd = commandWithStdin("");

        assertThrows(CommandLine.ParameterException.class, () -> parseArgs(cmd, "~/my-dotfiles", "--allow-post-install-scripts=yes"));
        verifyNoInteractions(configService);
    }

    @Test
    public void testCall_allowPostInstallMissingValue_throws() throws Exception {
        InitCommand cmd = commandWithStdin("");

        assertThrows(CommandLine.ParameterException.class, () -> parseArgs(cmd, "~/my-dotfiles", "--allow-post-install-scripts"));
        verifyNoInteractions(configService);
    }

    @Test
    public void testCall_initializeConfig_fileAlreadyExistsException_propagates() throws Exception {
        doThrow(new FileAlreadyExistsException("already exists"))
            .when(configService).initializeConfig(any(), eq(false));
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles");

        assertThrows(FileAlreadyExistsException.class, cmd::call);
        verify(configService, never()).printConfig();
    }

    @Test
    public void testCall_initializeConfig_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).initializeConfig(any(), eq(false));
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles");

        assertThrows(IOException.class, cmd::call);
        verify(configService, never()).printConfig();
    }

    @Test
    public void testCall_printConfig_ioException_propagates() throws Exception {
        doThrow(new IOException("disk full"))
            .when(configService).printConfig();
        InitCommand cmd = commandWithStdin("");
        parseArgs(cmd, "~/my-dotfiles");

        assertThrows(IOException.class, cmd::call);
        verify(configService).initializeConfig("~/my-dotfiles", false);
    }
}
