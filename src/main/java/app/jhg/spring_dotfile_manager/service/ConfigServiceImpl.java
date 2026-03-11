package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;
import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;

@Service
public class ConfigServiceImpl implements ConfigService {
    
    private final Path configFilePath;

    private final FileService fileService;

    private static final String DEFAULT_REPO_PATH = "{HOME}/dotfiles";

    public ConfigServiceImpl(
        @Value("${spring-dotfile-manager.config.path}") String configFilePath,
        FileService fileService
    ) {
        this.configFilePath = Path.of(configFilePath);
        this.fileService = fileService;
    }


    public void initializeConfig() throws FileExistsException, IOException {
        initializeConfig(DEFAULT_REPO_PATH);
    }

    public void initializeConfig(String dotfileRepoPath) throws FileExistsException, IOException {
        SDFMConfigModel config = new SDFMConfigModel(dotfileRepoPath);
        fileService.writeFile(configFilePath, config.getConfigFileContents());
    }
}
