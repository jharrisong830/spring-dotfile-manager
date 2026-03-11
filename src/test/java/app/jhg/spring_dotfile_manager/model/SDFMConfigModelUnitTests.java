package app.jhg.spring_dotfile_manager.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SDFMConfigModelUnitTests {
    
    @Test
    public void testGetConfigFileContents() {
        String repoPath = "/home/user/dotfiles";
        SDFMConfigModel configModel = new SDFMConfigModel(repoPath);

        String expectedContents = 
"""
dotfile-repo-path: %s
"""
            .formatted(repoPath);

        assertEquals(expectedContents, configModel.getConfigFileContents());
    }
}
