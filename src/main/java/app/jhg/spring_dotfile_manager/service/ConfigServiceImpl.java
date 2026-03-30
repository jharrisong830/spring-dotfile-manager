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
        FileService fileService,
        DotfileRepoPathMixin dotfileRepoPathMixin
    ) {
        String rawConfigPath = switch (FormattingUtils.getResolvedOsName(System.getProperty("os.name"))) {
            case "linux"  -> linuxPath;
            case "darwin" -> darwinPath;
            case "win32"  -> win32Path;
            default -> throw new UnsupportedOperationException("Unsupported OS: " + System.getProperty("os.name"));
        };
        this.configFilePath = Path.of(FormattingUtils.formatWithHomeDirectory(rawConfigPath));
        this.fileService = fileService;
        this.dotfileRepoPathMixin = dotfileRepoPathMixin;
    }

    @Override
    public void initializeConfig(String dotfileRepoPath) throws IOException {
        fileService.createDirectories(configFilePath.getParent());
        SDFMConfigModel config = new SDFMConfigModel(dotfileRepoPath);
        fileService.writeFile(configFilePath, config.getConfigFileContents());
    }

    @Override
    public String readConfig() throws IOException {
        String manualPath = dotfileRepoPathMixin.getDotfileRepoPath();
        if (manualPath != null && !manualPath.isBlank()) {
            log.debug("Using manually overridden dotfile repository path: '{}'", manualPath);
            return manualPath;
        }

        String configContent = fileService.readFile(configFilePath);
        SDFMConfigModel config = SDFMConfigModel.fromConfigFileContents(configContent);
        log.debug("Using dotfile repository path from config file: '{}'", config.dotfileRepoPath);
        return config.dotfileRepoPath;
    }

    @Override
    public void updateConfig(String newDotfileRepoPath) throws IOException {
        if (!fileService.exists(configFilePath)) {
            throw new FileNotFoundException("Configuration file does not exist at path: " + configFilePath);
        }
        SDFMConfigModel config = new SDFMConfigModel(newDotfileRepoPath);
        log.debug("Overwriting exisitng config file");
        fileService.overwriteFile(configFilePath, config.getConfigFileContents());
    }

    @Override
    public void printConfig() throws IOException {
        log.info("Configuration at: {}", getConfigFilePath());
        log.info("Using dotfile repository path: '{}'", FormattingUtils.formatWithHomeDirectory(readConfig()));
    }
}
