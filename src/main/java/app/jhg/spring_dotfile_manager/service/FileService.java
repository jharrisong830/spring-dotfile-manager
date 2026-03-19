package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {

    /**
     * checks if the given path exists (file or directory)
     * @param path the path to check
     * @return true if the path exists, false otherwise
     */
    public boolean exists(Path path);

    /**
     * checks if the given path is a directory
     * @param path the path to check
     * @return true if the path is a directory, false otherwise
     */
    public boolean isDirectory(Path path);

    /**
     * checks if the given path is a symbolic link
     * @param path the path to check
     * @return true if the path is a symbolic link, false otherwise
     */
    public boolean isSymbolicLink(Path path);

    /**
     * creates the directory at the given path, including any missing parent directories
     * @param path the path of the directory to create
     * @throws IOException if an I/O error occurs creating the directory
     */
    public void createDirectories(Path path) throws IOException;

    /**
     * writes the given content to a file at the specified path. If a file already exists at the path, a FileAlreadyExistsException is thrown.
     * @param path the path to write the file to
     * @param content the content to write to the file
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    public void writeFile(Path path, String content) throws IOException;

    /**
     * overwrites the file at the given path with the provided content. If no file exists at the path, a new file will be created.
     * @param path the path to write the file to
     * @param content the content to write to the file
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    public void overwriteFile(Path path, String content) throws IOException;
    
    /**
     * reads the content of a file at the specified path and returns it as a string
     * @param path the path to read the file from
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs reading from the file
     */
    public String readFile(Path path) throws IOException;

    /**
     * deletes the file at the specified path
     * @param path the path to the file to delete
     * @throws IOException if an I/O error occurs deleting the file
     */
    public void deleteFile(Path path) throws IOException;

    /**
     * performs a glob operation starting from the specified base directory and using the provided glob pattern, returning a list of matching file paths
     * @param baseDirectory
     * @param globPattern
     * @return a list of file paths that match the glob pattern starting from the base directory
     * @throws IOException if an I/O error occurs during the glob operation
     */
    public List<Path> glob(Path baseDirectory, String globPattern) throws IOException;
}
