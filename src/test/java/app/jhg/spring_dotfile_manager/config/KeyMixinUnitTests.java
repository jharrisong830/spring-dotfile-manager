package app.jhg.spring_dotfile_manager.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;

public class KeyMixinUnitTests {

    @Test
    public void testGetKey_returnsSetValue() {
        KeyMixin mixin = new KeyMixin();
        mixin.key = "bin";

        assertEquals("bin", mixin.getKey());
    }

    @Test
    public void testGetKey_whenNull_returnsNull() {
        KeyMixin mixin = new KeyMixin();

        assertNull(mixin.getKey());
    }

    @Test
    public void testParseArgs_keyOptionProvided_setsKey() {
        KeyMixin mixin = new KeyMixin();
        new CommandLine(mixin).parseArgs("--key", "bin");

        assertEquals("bin", mixin.getKey());
    }

    @Test
    public void testParseArgs_keyOptionNotProvided_leavesKeyNull() {
        KeyMixin mixin = new KeyMixin();
        new CommandLine(mixin).parseArgs();

        assertNull(mixin.getKey());
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

        assertEquals("   ", mixin.getKey());
    }
}
