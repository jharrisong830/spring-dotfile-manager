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

package app.jhg.spring_dotfile_manager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DotfileMarkerModelUnitTests {

    public static final String TEST_MARKER_FILENAME = "/home/testuser/dotfiles/.dotfiles";
    
    @Test
    public void testFromMarkerFileContents_singleMarker() {
        String markerFileContents = 
"""
name: .zshrc
location: /home/testuser/.zshrc
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());

        DotfileMarkerModel marker = markerModels.get(0);
        assertEquals(".zshrc", marker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), marker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), marker.sourceLocation);
        assertNull(marker.linuxOverride);
        assertNull(marker.win32Override);
        assertNull(marker.darwinOverride);
    }

    @Test
    public void testFromMarkerFileContents_multipleMarkers() {
        String markerFileContents =
"""
---
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
""";
        
        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(2, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), zshrcMarker.sourceLocation);
        assertNull(zshrcMarker.linuxOverride);
        assertNull(zshrcMarker.win32Override);
        assertNull(zshrcMarker.darwinOverride);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".vimrc"), vimrcMarker.sourceLocation);
        assertNull(vimrcMarker.linuxOverride);
        assertNull(vimrcMarker.win32Override);
        assertNull(vimrcMarker.darwinOverride);
    }

    @Test
    public void testFromMarkerFileContents_documentSplitInMiddle() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(2, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), zshrcMarker.sourceLocation);
        assertNull(zshrcMarker.linuxOverride);
        assertNull(zshrcMarker.win32Override);
        assertNull(zshrcMarker.darwinOverride);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".vimrc"), vimrcMarker.sourceLocation);
        assertNull(vimrcMarker.linuxOverride);
        assertNull(vimrcMarker.win32Override);
        assertNull(vimrcMarker.darwinOverride);
    }

    @Test
    public void testFromMarkerFileContents_documentSplitAtEnd() {
        String markerFileContents =
"""
---
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
---                
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(2, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), zshrcMarker.sourceLocation);
        assertNull(zshrcMarker.linuxOverride);
        assertNull(zshrcMarker.win32Override);
        assertNull(zshrcMarker.darwinOverride);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".vimrc"), vimrcMarker.sourceLocation);
        assertNull(vimrcMarker.linuxOverride);
        assertNull(vimrcMarker.win32Override);
        assertNull(vimrcMarker.darwinOverride);
    }

    @Test
    public void testFromMarkerFileContents_sequenceDocument() {
        String markerFileContents =
"""
- item1
- item2
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_scalarDocument() {
        String markerFileContents = "just a string\n";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_missingKey() {
        String markerFileContents =
"""
name: .zshrc
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_blankName() {
        String markerFileContents =
"""
name: ""
location: /home/testuser/.zshrc
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_blankLocation() {
        String markerFileContents =
"""
name: .zshrc
location: "   "
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_invalidKeyType() {
        String markerFileContents =
"""
name: .zshrc
location: 123
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_withPlatformOverrides() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    shouldLink: true
    location: /home/testuser/.linux_zshrc
win32:
    shouldLink: false
    location: C:/Users/testuser/.zshrc
darwin:
    shouldLink: true
    location: /Users/testuser/.zshrc                
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());

        DotfileMarkerModel marker = markerModels.get(0);
        assertEquals(".zshrc", marker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), marker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), marker.sourceLocation);

        assertTrue(marker.linuxOverride.shouldLink);
        assertEquals(Path.of("/home/testuser/.linux_zshrc"), marker.linuxOverride.location);

        assertFalse(marker.win32Override.shouldLink);
        assertNull(marker.win32Override.location);

        assertTrue(marker.darwinOverride.shouldLink);
        assertEquals(Path.of("/Users/testuser/.zshrc"), marker.darwinOverride.location);
    }

    @Test
    public void testFromMarkerFileContents_withPartialPlatformOverrides() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    shouldLink: true
    location: /home/testuser/.linux_zshrc
darwin:
    shouldLink: false
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());

        DotfileMarkerModel marker = markerModels.get(0);
        assertEquals(".zshrc", marker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), marker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), marker.sourceLocation);

        assertTrue(marker.linuxOverride.shouldLink);
        assertEquals(Path.of("/home/testuser/.linux_zshrc"), marker.linuxOverride.location);

        assertNull(marker.win32Override);

        assertFalse(marker.darwinOverride.shouldLink);
        assertNull(marker.darwinOverride.location);
    }

    @Test
    public void testFromMarkerFileContents_noLocationInPlatformOverride() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    shouldLink: true
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_blankLocationInPlatformOverride() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    shouldLink: true
    location: ""
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_whitespaceLocationInPlatformOverride() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    shouldLink: true
    location: "   "
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_missingShouldLink() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    location: /home/testuser/.linux_zshrc
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_invalidShouldLinkType() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
linux:
    shouldLink: "not_a_boolean"
    location: /home/testuser/.linux_zshrc
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_nameTemplatePlaceholder_expandsInBaseLocation() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/{NAME}
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());
        assertEquals(Path.of("/home/testuser/.zshrc"), markerModels.get(0).location);
    }

    @Test
    public void testFromMarkerFileContents_nameTemplatePlaceholder_expandsInPlatformOverrideLocation() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
darwin:
    shouldLink: true
    location: /Users/testuser/{NAME}
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());
        assertEquals(Path.of("/Users/testuser/.zshrc"), markerModels.get(0).darwinOverride.location);
    }

    @Test
    public void testFromMarkerFileContents_tildeExpansion_expandsInBaseLocation() {
        String markerFileContents =
"""
name: .zshrc
location: ~/.zshrc
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());
        assertEquals(Path.of(System.getProperty("user.home"), ".zshrc"), markerModels.get(0).location);
    }

    @Test
    public void testFromMarkerFileContents_multipleMarkersWithOverrides() {
        String markerFileContents =
"""
---
name: .zshrc
location: /home/testuser/.zshrc
win32:
    shouldLink: false
---
name: .vimrc
location: /home/testuser/.vimrc
darwin:
    shouldLink: true
    location: /Users/testuser/.config/.vimrc
win32:
    shouldLink: false
---
name: .gitconfig
location: /home/testuser/.gitconfig
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(3, markerModels.size());

        DotfileMarkerModel zshrcMarker = markerModels.get(0);
        assertEquals(".zshrc", zshrcMarker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), zshrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), zshrcMarker.sourceLocation);
        assertNull(zshrcMarker.linuxOverride);
        assertFalse(zshrcMarker.win32Override.shouldLink);
        assertNull(zshrcMarker.win32Override.location);
        assertNull(zshrcMarker.darwinOverride);

        DotfileMarkerModel vimrcMarker = markerModels.get(1);
        assertEquals(".vimrc", vimrcMarker.name);
        assertEquals(Path.of("/home/testuser/.vimrc"), vimrcMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".vimrc"), vimrcMarker.sourceLocation);
        assertNull(vimrcMarker.linuxOverride);
        assertFalse(vimrcMarker.win32Override.shouldLink);
        assertNull(vimrcMarker.win32Override.location);
        assertTrue(vimrcMarker.darwinOverride.shouldLink);
        assertEquals(Path.of("/Users/testuser/.config/.vimrc"), vimrcMarker.darwinOverride.location);

        DotfileMarkerModel gitconfigMarker = markerModels.get(2);
        assertEquals(".gitconfig", gitconfigMarker.name);
        assertEquals(Path.of("/home/testuser/.gitconfig"), gitconfigMarker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".gitconfig"), gitconfigMarker.sourceLocation);
        assertNull(gitconfigMarker.linuxOverride);
        assertNull(gitconfigMarker.win32Override);
        assertNull(gitconfigMarker.darwinOverride);
    }

    @Test
    public void testFromMarkerFileContents_key_defaultsToName() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());
        assertEquals(".zshrc", markerModels.get(0).key);
    }

    @Test
    public void testFromMarkerFileContents_key_explicitValueOverridesDefault() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
key: shell-config
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());

        DotfileMarkerModel marker = markerModels.get(0);
        assertEquals(".zshrc", marker.name);
        assertEquals("shell-config", marker.key);
    }

    @Test
    public void testFromMarkerFileContents_key_blank() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
key: ""
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_key_whitespaceOnly() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
key: "   "
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_key_invalidType() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/.zshrc
key: 123
""";

        assertThrows(IllegalArgumentException.class, () -> DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents));
    }

    @Test
    public void testFromMarkerFileContents_key_multipleMarkersEachDefaultToOwnName() {
        String markerFileContents =
"""
---
name: .zshrc
location: /home/testuser/.zshrc
---
name: .vimrc
location: /home/testuser/.vimrc
key: editor-config
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(2, markerModels.size());

        assertEquals(".zshrc", markerModels.get(0).key);
        assertEquals("editor-config", markerModels.get(1).key);
    }

    @Test
    public void testFromMarkerFileContents_key_doesNotAffectSourceLocationOrLocation() {
        String markerFileContents =
"""
name: .zshrc
location: /home/testuser/{NAME}
key: shell-config
""";

        List<DotfileMarkerModel> markerModels = DotfileMarkerModel.fromMarkerFileContents(Path.of(TEST_MARKER_FILENAME), markerFileContents);
        assertEquals(1, markerModels.size());

        DotfileMarkerModel marker = markerModels.get(0);
        assertEquals("shell-config", marker.key);
        assertEquals(".zshrc", marker.name);
        assertEquals(Path.of("/home/testuser/.zshrc"), marker.location);
        assertEquals(Path.of(TEST_MARKER_FILENAME).getParent().resolve(".zshrc"), marker.sourceLocation);
    }
}