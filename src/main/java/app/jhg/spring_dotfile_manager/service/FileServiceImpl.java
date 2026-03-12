package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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

    @Override
    public List<Path> glob(Path baseDirectory, String globPattern) throws IOException {
        if (!Files.exists(baseDirectory)) {
            throw new IOException("Base directory does not exist: " + baseDirectory);
        }
        if (!Files.isDirectory(baseDirectory)) {
            throw new IOException("Base directory is not a directory: " + baseDirectory);
        }

        String fullGlobPattern = "file:" + baseDirectory + "/" + globPattern;

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(fullGlobPattern);
        
        List<Path> markerPaths = new ArrayList<>();
        for (Resource resource : resources) {
            markerPaths.add(resource.getFilePath());
        }

        return markerPaths;
    }
}
