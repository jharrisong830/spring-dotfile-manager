package app.jhg.spring_dotfile_manager.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DotfileRepoPathMixinUnitTests {

    @Test
    public void testGetDotfileRepoPath_returnsSetValue() {
        DotfileRepoPathMixin mixin = new DotfileRepoPathMixin();
        mixin.dotfileRepoPath = "/path/to/dotfiles";

        assertEquals("/path/to/dotfiles", mixin.getDotfileRepoPath());
    }

    @Test
    public void testGetDotfileRepoPath_whenNull_returnsNull() {
        DotfileRepoPathMixin mixin = new DotfileRepoPathMixin();

        assertNull(mixin.getDotfileRepoPath());
    }

    @Test
    public void testGetDotfileRepoPath_withHomeExpansion_returnsSetValue() {
        DotfileRepoPathMixin mixin = new DotfileRepoPathMixin();
        mixin.dotfileRepoPath = "~/dotfiles";

        assertEquals("~/dotfiles", mixin.getDotfileRepoPath());
    }

    @Test
    public void testGetDotfileRepoPath_withBlankValue_returnsBlankValue() {
        DotfileRepoPathMixin mixin = new DotfileRepoPathMixin();
        mixin.dotfileRepoPath = "   ";

        assertEquals("   ", mixin.getDotfileRepoPath());
    }

    @Test
    public void testGetDotfileRepoPath_withEmptyString_returnsEmptyString() {
        DotfileRepoPathMixin mixin = new DotfileRepoPathMixin();
        mixin.dotfileRepoPath = "";

        assertEquals("", mixin.getDotfileRepoPath());
    }
}
