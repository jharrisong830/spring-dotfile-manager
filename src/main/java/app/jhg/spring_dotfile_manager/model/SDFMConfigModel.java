package app.jhg.spring_dotfile_manager.model;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import lombok.Getter;

public class SDFMConfigModel {
    
    @Getter
    private String dotfileRepoPath;

    public SDFMConfigModel(String dotfileRepoPath) {
        this.dotfileRepoPath = dotfileRepoPath;
    }

    public String getConfigFileContents() {
        Yaml yaml = new Yaml();
        Map<String, String> configFileMap = Map.of("dotfile-repo-path", dotfileRepoPath);
        return yaml.dumpAsMap(configFileMap);
    }

    public static SDFMConfigModel fromConfigFileContents(String configFileContents) {
        Yaml yaml = new Yaml();
        Map<String, Object> configFileMap;
        
        try {
            configFileMap = yaml.load(configFileContents);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid configuration file contents: expected a YAML mapping", e);
        }

        if (configFileMap == null || !configFileMap.containsKey("dotfile-repo-path")) {
            throw new IllegalArgumentException("Invalid configuration file contents: missing 'dotfile-repo-path' key");
        }

        Object rawValue = configFileMap.get("dotfile-repo-path");
        if (rawValue == null) {
            throw new IllegalArgumentException("Invalid configuration file contents: 'dotfile-repo-path' value is missing");
        }
        if (!(rawValue instanceof String dotfileRepoPath) || dotfileRepoPath.isBlank()) {
            throw new IllegalArgumentException("Invalid configuration file contents: 'dotfile-repo-path' must be a non-blank string");
        }
        return new SDFMConfigModel(dotfileRepoPath);
    }
}
