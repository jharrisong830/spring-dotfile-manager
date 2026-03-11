package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;

public interface ConfigService {

    /**
     * Initializes the configuration file with the provided dotfile repository path. If the configuration file already exists, a FileAlreadyExistsException is thrown. If an I/O error occurs during file operations, an IOException is thrown.
     * @param dotfileRepoPath The path to the user's dotfile repository to be stored in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    public void initializeConfig(String dotfileRepoPath) throws IOException;
    
    /**
     * Reads the configuration file and returns the path to the user's dotfile repository. If an I/O error occurs during file operations, an IOException is thrown.
     * @return The path to the user's dotfile repository as specified in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    public String readConfig() throws IOException;

    /**
     * Updates the configuration file with a new dotfile repository path. If an I/O error occurs during file operations, an IOException is thrown. If the configuration file does not exist, a FileNotFoundException is thrown.
     * @param newDotfileRepoPath The new path to the user's dotfile repository to be updated in the configuration file.
     * @throws IOException if an I/O error occurs during file operations.
     */
    public void updateConfig(String newDotfileRepoPath) throws IOException;
}
