package app.jhg.spring_dotfile_manager.model;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class SDFMConfigModel {
    
    private String dotfileRepoPath;

    public SDFMConfigModel(String dotfileRepoPath) {
        this.dotfileRepoPath = dotfileRepoPath;
    }

    public String getConfigFileContents() {
        Yaml yaml = new Yaml();
        Map<String, String> configFileMap = Map.of("dotfile-repo-path", dotfileRepoPath);
        return yaml.dumpAsMap(configFileMap);
    }
}
