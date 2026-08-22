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
