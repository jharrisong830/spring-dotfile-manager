package app.jhg.spring_dotfile_manager.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
