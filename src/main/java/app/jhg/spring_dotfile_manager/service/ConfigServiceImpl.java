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

    public ConfigServiceImpl(
        @Value("${spring-dotfile-manager.config.path}") String configFilePath,
        FileService fileService
    ) {
        this.configFilePath = Path.of(configFilePath);
        this.fileService = fileService;
    }

    /**
     * Initializes the configuration file with the provided dotfile repository path. If the configuration file already exists, a FileExistsException is thrown. If an I/O error occurs during file operations, an IOException is thrown.
     * @param dotfileRepoPath The path to the user's dotfile repository to be stored in the configuration file.
     * @throws FileExistsException if the configuration file already exists.
     * @throws IOException if an I/O error occurs during file operations.
     */
    @Override
    public void initializeConfig(String dotfileRepoPath) throws FileExistsException, IOException {
        fileService.createDirectories(configFilePath.getParent());
        SDFMConfigModel config = new SDFMConfigModel(dotfileRepoPath);
        fileService.writeFile(configFilePath, config.getConfigFileContents());
    }
}
