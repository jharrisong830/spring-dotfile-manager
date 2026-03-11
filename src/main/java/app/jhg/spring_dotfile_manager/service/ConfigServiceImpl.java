package app.jhg.spring_dotfile_manager.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.SDFMConfigModel;

@Service
public class ConfigServiceImpl implements ConfigService {
    
    private final Path configFilePath;

    private final FileService fileService;

    public ConfigServiceImpl(
        @Value("${spring-dotfile-manager.config.path}") String configFilePath,
        FileService fileService
    ) {
        this.configFilePath = Path.of(configFilePath.replaceAll("\\{HOME\\}", System.getProperty("user.home")));
        this.fileService = fileService;
    }

    /**
     * Initializes the configuration file with the provided dotfile repository path. If the configuration file already exists, a FileAlreadyExistsException is thrown. If an I/O error occurs during file operations, an IOException is thrown.
     * @param dotfileRepoPath The path to the user's dotfile repository to be stored in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    @Override
    public void initializeConfig(String dotfileRepoPath) throws IOException {
        fileService.createDirectories(configFilePath.getParent());
        SDFMConfigModel config = new SDFMConfigModel(dotfileRepoPath);
        fileService.writeFile(configFilePath, config.getConfigFileContents());
    }

    /**
     * Reads the configuration file and returns the path to the user's dotfile repository. If an I/O error occurs during file operations, an IOException is thrown.
     * @return The path to the user's dotfile repository as specified in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    @Override
    public String readConfig() throws IOException {
        String configContent = fileService.readFile(configFilePath);
        SDFMConfigModel config = SDFMConfigModel.fromConfigFileContents(configContent);
        return config.dotfileRepoPath;
    }

    /**
     * Updates the configuration file with a new dotfile repository path. If an I/O error occurs during file operations, an IOException is thrown. If the configuration file does not exist, a FileNotFoundException is thrown.
     * @param newDotfileRepoPath The new path to the user's dotfile repository to be updated in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    @Override
    public void updateConfig(String newDotfileRepoPath) throws IOException {
        if (!fileService.exists(configFilePath)) {
            throw new FileNotFoundException("Configuration file does not exist at path: " + configFilePath);
        }
        SDFMConfigModel config = new SDFMConfigModel(newDotfileRepoPath);
        fileService.overwriteFile(configFilePath, config.getConfigFileContents());
    }
}
