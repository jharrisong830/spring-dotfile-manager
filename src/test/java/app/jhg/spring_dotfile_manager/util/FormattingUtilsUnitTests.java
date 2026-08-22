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

package app.jhg.spring_dotfile_manager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class FormattingUtilsUnitTests {

    private static final String HOME = System.getProperty("user.home");

    @Test
    public void testFormatWithHomeDirectory() {
        String input = "{HOME}/dotfiles";
        String expectedOutput = HOME + "/dotfiles";

        assertEquals(expectedOutput, FormattingUtils.formatWithHomeDirectory(input));
    }

    @Test
    public void testFormatWithHomeDirectory_noPlaceholder() {
        String input = "/bin/dotfiles";

        assertEquals(input, FormattingUtils.formatWithHomeDirectory(input));
    }

    @Test
    public void testFormatWithHomeDirectory_tildePlaceholder() {
        String input = "~/dotfiles";
        String expectedOutput = HOME + "/dotfiles";

        assertEquals(expectedOutput, FormattingUtils.formatWithHomeDirectory(input));
    }

    @Test
    public void testFormatWithHomeDirectory_tildeNotAtStart() {
        String input = "/path/to/~/dotfiles";

        assertEquals(input, FormattingUtils.formatWithHomeDirectory(input));
    }

    @Test
    public void testFormatWithHomeDirectory_tildeAndHome() {
        String input = "~{HOME}/dotfiles";
        String expectedOutput = HOME + HOME + "/dotfiles";

        assertEquals(expectedOutput, FormattingUtils.formatWithHomeDirectory(input));
    }

    @Test
    public void testFormatWithHomeDirectory_multipleHomePlaceholders() {
        String input = "{HOME}/dotfiles:~/.config:{HOME}/.local";
        String expectedOutput = HOME + "/dotfiles:~/.config:" + HOME + "/.local";

        assertEquals(expectedOutput, FormattingUtils.formatWithHomeDirectory(input));
    }

    @Test
    public void testFormatWithHomeDirectory_emptyString() {
        assertEquals("", FormattingUtils.formatWithHomeDirectory(""));
    }

    @Test
    public void testFormatWithHomeDirectory_otherPlaceholders() {
        String input = "{OTHER_VAR}/.config";

        assertEquals(input, FormattingUtils.formatWithHomeDirectory(input));
    }


    @Test
    public void testFormatWithFilename() {
        assertEquals("/home/user/.zshrc", FormattingUtils.formatWithName("/home/user/{NAME}", ".zshrc"));
    }

    @Test
    public void testFormatWithFilename_multiplePlaceholders() {
        assertEquals(".zshrc/.zshrc", FormattingUtils.formatWithName("{NAME}/{NAME}", ".zshrc"));
    }

    @Test
    public void testFormatWithFilename_noPlaceholder() {
        String input = "/home/user/.zshrc";
        assertEquals(input, FormattingUtils.formatWithName(input, ".zshrc"));
    }

    @Test
    public void testFormatWithFilename_emptyString() {
        assertEquals("", FormattingUtils.formatWithName("", ".zshrc"));
    }

    @Test
    public void testFormatWithFilename_nameContainsRegexSpecialCharacters() {
        assertEquals("/home/user/.zsh$rc", FormattingUtils.formatWithName("/home/user/{NAME}", ".zsh$rc"));
    }


    @Test
    public void testGetResolvedOsName_linux() {
        assertEquals(Os.LINUX, FormattingUtils.getResolvedOsName("Linux"));
    }

    @Test
    public void testGetResolvedOsName_linux_fullName() {
        assertEquals(Os.LINUX, FormattingUtils.getResolvedOsName("Linux 5.15.0-generic"));
    }

    @Test
    public void testGetResolvedOsName_darwin() {
        assertEquals(Os.DARWIN, FormattingUtils.getResolvedOsName("Mac OS X"));
    }

    @Test
    public void testGetResolvedOsName_win32() {
        assertEquals(Os.WIN32, FormattingUtils.getResolvedOsName("Windows 10"));
    }

    @Test
    public void testGetResolvedOsName_win32_nt() {
        assertEquals(Os.WIN32, FormattingUtils.getResolvedOsName("Windows NT 10.0"));
    }

    @Test
    public void testGetResolvedOsName_unsupported() {
        assertThrows(UnsupportedOperationException.class, () -> FormattingUtils.getResolvedOsName("FreeBSD"));
    }

    @Test
    public void testGetResolvedOsName_unsupported_messageContainsOsName() {
        String osName = "FreeBSD";
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> FormattingUtils.getResolvedOsName(osName)
        );
        assertEquals(true, ex.getMessage().contains(osName));
    }
}
