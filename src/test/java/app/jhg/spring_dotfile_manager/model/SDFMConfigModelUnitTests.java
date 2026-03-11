package app.jhg.spring_dotfile_manager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SDFMConfigModelUnitTests {
    
    @Test
    public void testGetConfigFileContents() {
        String repoPath = "/home/user/dotfiles";
        SDFMConfigModel configModel = new SDFMConfigModel(repoPath);

        String expectedContents = "dotfile-repo-path: %s"
            .formatted(repoPath);

        assertEquals(expectedContents, configModel.getConfigFileContents().trim());
    }

    @Test
    public void testFromConfigFileContents() {
        String repoPath = "/home/user/dotfiles";
        String configFileContents = "dotfile-repo-path: %s"            
            .formatted(repoPath);
        
        SDFMConfigModel configModel = SDFMConfigModel.fromConfigFileContents(configFileContents);

        assertEquals(repoPath, configModel.getDotfileRepoPath());
        assertEquals(configFileContents, configModel.getConfigFileContents().trim());
    }

    @Test
    public void testFromConfigFileContents_missingKey() {
        String configFileContents = "invalid-key: /home/user/dotfiles";
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