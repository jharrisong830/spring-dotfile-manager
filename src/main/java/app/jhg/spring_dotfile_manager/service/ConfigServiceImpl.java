package app.jhg.spring_dotfile_manager.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;
import lombok.Getter;

@Service
public class ConfigServiceImpl implements ConfigService {
    
    @Getter
    private final Path configFilePath;

    private final FileService fileService;

    public ConfigServiceImpl(
        @Value("${spring-dotfile-manager.config.path}") String configFilePath,
        FileService fileService,
        FormatterService formatterService
    ) {
        this.configFilePath = Path.of(formatterService.formatWithHomeDirectory(configFilePath));
        this.fileService = fileService;
    }

    @Override
    public void initializeConfig(String dotfileRepoPath) throws IOException {
        fileService.createDirectories(configFilePath.getParent());
        SDFMConfigModel config = new SDFMConfigModel(dotfileRepoPath);
        fileService.writeFile(configFilePath, config.getConfigFileContents());
    }

    @Override
    public String readConfig() throws IOException {
        String configContent = fileService.readFile(configFilePath);
        SDFMConfigModel config = SDFMConfigModel.fromConfigFileContents(configContent);
        return config.dotfileRepoPath;
    }

    @Override
    public void updateConfig(String newDotfileRepoPath) throws IOException {
        if (!fileService.exists(configFilePath)) {
            throw new FileNotFoundException("Configuration file does not exist at path: " + configFilePath);
        }
        SDFMConfigModel config = new SDFMConfigModel(newDotfileRepoPath);
        fileService.overwriteFile(configFilePath, config.getConfigFileContents());
    }
}
