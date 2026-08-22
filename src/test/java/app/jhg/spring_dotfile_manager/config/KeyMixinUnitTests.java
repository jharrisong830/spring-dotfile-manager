package app.jhg.spring_dotfile_manager.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;

public class KeyMixinUnitTests {

    @Test
    public void testGetKeys_returnsSetValue() {
        KeyMixin mixin = new KeyMixin();
        mixin.keys = List.of("bin");

        assertEquals(List.of("bin"), mixin.getKeys());
    }

    @Test
    public void testGetKeys_whenNotSet_returnsEmptyList() {
        KeyMixin mixin = new KeyMixin();

        assertTrue(mixin.getKeys().isEmpty());
    }

    @Test
    public void testParseArgs_keyOptionProvided_setsKeys() {
        KeyMixin mixin = new KeyMixin();
        new CommandLine(mixin).parseArgs("--key", "bin");

        assertEquals(List.of("bin"), mixin.getKeys());
    }

    @Test
    public void testParseArgs_keyOptionProvidedMultipleTimes_setsAllKeys() {
        KeyMixin mixin = new KeyMixin();
        new CommandLine(mixin).parseArgs("--key", "bin", "--key", "vim");

        assertEquals(List.of("bin", "vim"), mixin.getKeys());
    }

    @Test
    public void testParseArgs_keyOptionNotProvided_leavesKeysEmpty() {
        KeyMixin mixin = new KeyMixin();
        new CommandLine(mixin).parseArgs();

        assertTrue(mixin.getKeys().isEmpty());
    }

    @Test
    public void testParseArgs_keyOptionMissingValue_throwsParameterException() {
        KeyMixin mixin = new KeyMixin();

        assertThrows(CommandLine.ParameterException.class, () -> new CommandLine(mixin).parseArgs("--key"));
    }

    @Test
    public void testParseArgs_keyOptionGivenBlankValue_setsBlankKey() {
        KeyMixin mixin = new KeyMixin();
        new CommandLine(mixin).parseArgs("--key", "   ");

        assertEquals(List.of("   "), mixin.getKeys());
    }
}
