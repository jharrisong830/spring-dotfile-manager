package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {
    
    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    @Override
    public boolean isSymbolicLink(Path path) {
        return Files.isSymbolicLink(path);
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    @Override
    public void writeFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public void overwriteFile(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    @Override
    public String readFile(Path path) throws IOException {
        return Files.readString(path);
    }
}
