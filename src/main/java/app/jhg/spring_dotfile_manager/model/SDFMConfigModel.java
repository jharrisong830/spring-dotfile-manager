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
