package app.jhg.spring_dotfile_manager.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import app.jhg.spring_dotfile_manager.config.DotfileRepoPathMixin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfigServiceImpl implements ConfigService {
    
    @Getter
    private final Path configFilePath;

    private final FileService fileService;
    
    private final DotfileRepoPathMixin dotfileRepoPathMixin;

    public ConfigServiceImpl(
        @Value("${spring-dotfile-manager.config.path.linux}") String linuxPath,
        @Value("${spring-dotfile-manager.config.path.darwin}") String darwinPath,
        @Value("${spring-dotfile-manager.config.path.win32}") String win32Path,
        @Value("${os.name}") String osName,
        FileService fileService,
        DotfileRepoPathMixin dotfileRepoPathMixin
    ) {
        String rawConfigPath = switch (FormattingUtils.getResolvedOsName(osName)) {
            case LINUX  -> linuxPath;
            case DARWIN -> darwinPath;
            case WIN32  -> win32Path;
        };
        this.configFilePath = Path.of(FormattingUtils.formatWithHomeDirectory(rawConfigPath));
        this.fileService = fileService;
        this.dotfileRepoPathMixin = dotfileRepoPathMixin;
    }

    @Override
    public void initializeConfig(String dotfileRepoPath, boolean allowPostInstallScripts) throws IOException {
        fileService.createDirectories(configFilePath.getParent());
        SDFMConfigModel config = new SDFMConfigModel(dotfileRepoPath, allowPostInstallScripts);
        fileService.writeFile(configFilePath, config.getConfigFileContents());
    }

    @Override
    public String readDotfileRepoPath() throws IOException {
        String manualPath = dotfileRepoPathMixin.getDotfileRepoPath();
        if (manualPath != null && !manualPath.isBlank()) {
            log.debug("Using manually overridden dotfile repository path: '{}'", manualPath);
            return manualPath;
        }

        SDFMConfigModel config = loadConfig();
        log.debug("Using dotfile repository path from config file: '{}'", config.dotfileRepoPath);
        return config.dotfileRepoPath;
    }

    @Override
    public boolean readAllowPostInstallScripts() throws IOException {
        return loadConfig().allowPostInstallScripts;
    }

    private SDFMConfigModel loadConfig() throws IOException {
        String configContent = fileService.readFile(configFilePath);
        return SDFMConfigModel.fromConfigFileContents(configContent);
    }

    @Override
    public void updateConfig(String newDotfileRepoPath, boolean newAllowPostInstallScripts) throws IOException {
        if (!fileService.exists(configFilePath)) {
            throw new FileNotFoundException("Configuration file does not exist at path: " + configFilePath);
        }
        SDFMConfigModel config = new SDFMConfigModel(newDotfileRepoPath, newAllowPostInstallScripts);
        log.debug("Overwriting existing config file");
        fileService.overwriteFile(configFilePath, config.getConfigFileContents());
    }

    @Override
    public void printConfig() throws IOException {
        log.info("Configuration at: {}", getConfigFilePath());
        log.info("Using dotfile repository path: '{}'", FormattingUtils.formatWithHomeDirectory(readDotfileRepoPath()));
        log.info("Allow post-install scripts? {}", readAllowPostInstallScripts());
    }
}
