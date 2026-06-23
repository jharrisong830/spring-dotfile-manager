package app.jhg.spring_dotfile_manager.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class SDFMConfigModel {
    
    public final String dotfileRepoPath;
    public final boolean allowPostInstallScripts;

    public SDFMConfigModel(String dotfileRepoPath, boolean allowPostInstallScripts) {
        this.dotfileRepoPath = dotfileRepoPath;
        this.allowPostInstallScripts = allowPostInstallScripts;
    }

    public String getConfigFileContents() {
        Yaml yaml = new Yaml();
        Map<String, Object> configFileMap = new LinkedHashMap<>();
        configFileMap.put("dotfile-repo-path", dotfileRepoPath);
        configFileMap.put("allow-post-install-scripts", allowPostInstallScripts);
        return yaml.dumpAsMap(configFileMap);
    }

    public static SDFMConfigModel fromConfigFileContents(String configFileContents) {
        Yaml yaml = new Yaml();
        Map<String, Object> configFileMap;
        
        try {
            configFileMap = yaml.load(configFileContents);
        } catch (YAMLException | ClassCastException e) {
            throw new IllegalArgumentException("Invalid configuration file contents: expected a YAML mapping", e);
        }

        if (configFileMap == null) {
            throw new IllegalArgumentException("Invalid configuration file contents: could not parse config file");
        }

        Object dotfileRepoPathRawValue = configFileMap.get("dotfile-repo-path");
        if (dotfileRepoPathRawValue == null) {
            throw new IllegalArgumentException("Invalid configuration file contents: 'dotfile-repo-path' value is missing");
        }
        if (!(dotfileRepoPathRawValue instanceof String dotfileRepoPath) || dotfileRepoPath.isBlank()) {
            throw new IllegalArgumentException("Invalid configuration file contents: 'dotfile-repo-path' must be a non-blank string");
        }

        Object allowPostInstallScriptsRawValue = configFileMap.get("allow-post-install-scripts");
        if (allowPostInstallScriptsRawValue == null) {
            throw new IllegalArgumentException("Invalid configuration file contents: 'allow-post-install-scripts' value is missing");
        }
        if (!(allowPostInstallScriptsRawValue instanceof Boolean allowPostInstallScripts)) {
            throw new IllegalArgumentException("Invalid configuration file contents: 'allow-post-install-scripts' must be a boolean");
        }

        return new SDFMConfigModel(dotfileRepoPath, allowPostInstallScripts);
    }
}
