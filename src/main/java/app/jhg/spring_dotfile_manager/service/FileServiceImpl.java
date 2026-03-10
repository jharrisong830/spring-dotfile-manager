package app.jhg.spring_dotfile_manager.service;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

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

    public boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }
}
