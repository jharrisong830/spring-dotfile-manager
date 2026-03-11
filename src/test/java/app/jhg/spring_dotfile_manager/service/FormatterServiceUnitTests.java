package app.jhg.spring_dotfile_manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FormatterServiceUnitTests {
    
    private final FormatterService formatterService = new FormatterServiceImpl("/home/user");

    @Test
    public void testFormatWithHomeDirectory() {
        String input = "{HOME}/dotfiles";
        String expectedOutput = "/home/user/dotfiles";

        String actualOutput = formatterService.formatWithHomeDirectory(input);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testFormatWithHomeDirectory_noPlaceholder() {
        String input = "/bin/dotfiles";
        String expectedOutput = "/bin/dotfiles";

        String actualOutput = formatterService.formatWithHomeDirectory(input);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testFormatWithHomeDirectory_tildePlaceholder() {
        String input = "~/dotfiles";
        String expectedOutput = "/home/user/dotfiles";

        String actualOutput = formatterService.formatWithHomeDirectory(input);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testFormatWithHomeDirectory_multiplePlaceholders() {
        String input = "{HOME}/dotfiles:~/.config:{HOME}/.local";
        String expectedOutput = "/home/user/dotfiles:/home/user/.config:/home/user/.local";

        String actualOutput = formatterService.formatWithHomeDirectory(input);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testFormatWithHomeDirectory_emptyString() {
        String input = "";
        String expectedOutput = "";

        String actualOutput = formatterService.formatWithHomeDirectory(input);

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testFormatWithHomeDirectory_otherPlaceholders() {
        String input = "{OTHER_VAR}/.config";
        String expectedOutput = "{OTHER_VAR}/.config";

        String actualOutput = formatterService.formatWithHomeDirectory(input);

        assertEquals(expectedOutput, actualOutput);
    }
}
