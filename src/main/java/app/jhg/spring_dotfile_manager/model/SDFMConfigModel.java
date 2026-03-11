package app.jhg.spring_dotfile_manager.model;

public class SDFMConfigModel {
    
    private String dotfileRepoPath;

    public SDFMConfigModel(String dotfileRepoPath) {
        this.dotfileRepoPath = dotfileRepoPath;
    }

    public String getConfigFileContents() {
        return String.format("""
            dotfile-repo-path: "%s"
            """, this.dotfileRepoPath);
    }
}
