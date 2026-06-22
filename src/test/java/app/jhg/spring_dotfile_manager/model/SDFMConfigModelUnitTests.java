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