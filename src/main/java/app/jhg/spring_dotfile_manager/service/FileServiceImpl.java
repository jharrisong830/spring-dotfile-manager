package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {
    
    /**
     * checks if the given path exists (file or directory)
     * @param path the path to check
     * @return true if the path exists, false otherwise
     */
    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * checks if the given path is a directory
     * @param path the path to check
     * @return true if the path is a directory, false otherwise
     */
    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * checks if the given path is a symbolic link
     * @param path the path to check
     * @return true if the path is a symbolic link, false otherwise
     */
    @Override
    public boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }

    /**
     * creates the directory at the given path, including any missing parent directories
     * @param path the path of the directory to create
     * @throws IOException if an I/O error occurs creating the directory
     */
    @Override
    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    /**
     * writes the given content to a file at the specified path. If a file already exists at the path, a FileAlreadyExistsException is thrown.
     * @param path the path to write the file to
     * @param content the content to write to the file
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    @Override
    public void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
    }

    /**
     * overwrites the file at the given path with the provided content. If no file exists at the path, a new file will be created.
     * @param path the path to write the file to
     * @param content the content to write to the file
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    @Override
    public void overwriteFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    /**
     * reads the content of a file at the specified path and returns it as a string
     * @param path the path to read the file from
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs reading from the file
     */
    @Override
    public String readFile(Path path) throws IOException {
        return Files.readString(path);
    }
}
