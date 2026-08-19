package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;

public interface ConfigService {

    Path getConfigFilePath();

    /**
     * Initializes the configuration file with the provided dotfile repository path and postinstall preference. If the configuration file already exists, a FileAlreadyExistsException is thrown. If an I/O error occurs during file operations, an IOException is thrown.
     * @param dotfileRepoPath The path to the user's dotfile repository to be stored in the configuration file.
     * @param allowPostInstallScripts whether to allow postinstall scripts to run 
     * @throws IOException if an I/O error occurs during file operations.
     */
    void initializeConfig(String dotfileRepoPath, boolean allowPostInstallScripts) throws IOException;
    
    /**
     * Reads the configuration file and returns the path to the user's dotfile repository. If an I/O error occurs during file operations, an IOException is thrown.
     * @return The path to the user's dotfile repository as specified in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    String readDotfileRepoPath() throws IOException;

    /**
     * Reads the configuration file and returns the post install script preference. If an I/O error occurs during file operations, an IOException is thrown.
     * @return The user's preference for running post-install scripts
     * @throws IOException if an I/O error occurs during file operations.
     */
    boolean readAllowPostInstallScripts() throws IOException;

    /**
     * Updates the configuration file with a new dotfile repository path and postinstall preference. If an I/O error occurs during file operations, an IOException is thrown. If the configuration file does not exist, a FileNotFoundException is thrown.
     * @param newDotfileRepoPath The new path to the user's dotfile repository to be updated in the configuration file.
     * @param newAllowPostInstallScripts new preference for whether to allow postinstall scripts to run
     * @throws IOException if an I/O error occurs during file operations.
     */
    void updateConfig(String newDotfileRepoPath, boolean newAllowPostInstallScripts) throws IOException;

    /**
     * Prints the current configuration, including the path to the configuration file and the dotfile repository path and postinstall preference specified in the configuration. If an I/O error occurs during file operations, an IOException is thrown.
     * @throws IOException if an I/O error occurs during file operations.
     */
    void printConfig() throws IOException;
}
