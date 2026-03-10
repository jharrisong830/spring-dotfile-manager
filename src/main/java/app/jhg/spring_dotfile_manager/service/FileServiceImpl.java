package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;

@Service
public class FileServiceImpl implements FileService {
    
    /**
     * checks if the given path exists (file or directory)
     * @param path the path to check
     * @return true if the path exists, false otherwise
     */
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * checks if the given path is a directory
     * @param path the path to check
     * @return true if the path is a directory, false otherwise
     */
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * checks if the given path is a symbolic link
     * @param path the path to check
     * @return true if the path is a symbolic link, false otherwise
     */
    public boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }

    /**
     * writes the given content to a file at the specified path. If a file already exists at the path, a FileExistsException is thrown.
     * @param path the path to write the file to
     * @param content the content to write to the file
     * @throws FileExistsException if a file already exists at the specified path
     * @throws IOException if an I/O error occurs writing to or creating the file
     */
    public void writeFile(Path path, String content) throws FileExistsException, IOException {
        if (exists(path)) {
            throw new FileExistsException("File already exists at path '" + path.toString() + "'");
        }

        Files.writeString(path, content);
    }

    /**
     * reads the content of a file at the specified path and returns it as a string
     * @param path the path to read the file from
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs reading from the file
     */
    public String readFile(Path path) throws IOException {
        return Files.readString(path);
    }
}
