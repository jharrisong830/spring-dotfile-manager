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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SDFMConfigModelUnitTests {
    
    @Test
    public void testGetConfigFileContents() {
        String repoPath = "/home/user/dotfiles";
        boolean allowPostInstall = false;
        SDFMConfigModel configModel = new SDFMConfigModel(repoPath, allowPostInstall);

        String expectedContents = "dotfile-repo-path: %s\nallow-post-install-scripts: %b\n"
            .formatted(repoPath, allowPostInstall);

        assertEquals(expectedContents, configModel.getConfigFileContents());
    }

    @Test
    public void testFromConfigFileContents() {
        String repoPath = "/home/user/dotfiles";
        boolean allowPostInstall = false;
        String configFileContents = "dotfile-repo-path: %s\nallow-post-install-scripts: %b\n"
            .formatted(repoPath, allowPostInstall);

        SDFMConfigModel configModel = SDFMConfigModel.fromConfigFileContents(configFileContents);

        assertEquals(repoPath, configModel.dotfileRepoPath);
        assertEquals(allowPostInstall, configModel.allowPostInstallScripts);
        assertEquals(configFileContents, configModel.getConfigFileContents());
    }

    @Test
    public void testFromConfigFileContents_missingKey() {
        String configFileContents = "dotfile-repo-path: /home/user/dotfiles"; // missing postinstall flag
        assertThrows(IllegalArgumentException.class, () -> SDFMConfigModel.fromConfigFileContents(configFileContents));
    }

    @Test
    public void testFromConfigFileContents_emptyValue() {
        String configFileContents = "";
        assertThrows(IllegalArgumentException.class, () -> SDFMConfigModel.fromConfigFileContents(configFileContents));
    }

    @Test
    public void testFromConfigFileContents_nonMapValue() {
        String configFileContents = "not-a-map";
        assertThrows(IllegalArgumentException.class, () -> SDFMConfigModel.fromConfigFileContents(configFileContents));
    }

    @Test
    public void testFromConfigFileContents_nullValue() {
        String configFileContents = "dotfile-repo-path:";
        assertThrows(IllegalArgumentException.class, () -> SDFMConfigModel.fromConfigFileContents(configFileContents));
    }

    @Test
    public void testFromConfigFileContents_blankValue() {
        String configFileContents = "dotfile-repo-path: \"\"";
        assertThrows(IllegalArgumentException.class, () -> SDFMConfigModel.fromConfigFileContents(configFileContents));
    }

    @Test
    public void testFromConfigFileContents_nonStringValue() {
        String configFileContents = "dotfile-repo-path: 12345";
        assertThrows(IllegalArgumentException.class, () -> SDFMConfigModel.fromConfigFileContents(configFileContents));
    }
}