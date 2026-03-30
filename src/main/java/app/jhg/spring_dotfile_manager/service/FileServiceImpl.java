package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
        log.debug("Creating directories along path: {}", path);
        Files.createDirectories(path);
    }

    @Override
    public void createSymlink(Path linkPath, Path source) throws IOException {
        log.debug("Creating symlink: {} -> {}", source, linkPath);
        Files.createSymbolicLink(linkPath, source);
    }

    @Override
    public void writeFile(Path path, String content) throws IOException {
        log.debug("Writing NEW file: {}", path);
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public void overwriteFile(Path path, String content) throws IOException {
        log.debug("OVERWRITING file: {}", path);
        Files.writeString(path, content);
    }

    @Override
    public String readFile(Path path) throws IOException {
        log.debug("Reading file: {}", path);
        return Files.readString(path);
    }

    @Override
    public void deleteFile(Path path) throws IOException {
        log.debug("Deleting file/directory: {}", path);
        Files.delete(path);
    }

    @Override
    public void forceDelete(Path path) throws IOException {
        log.debug("FORCE DELETE: {}", path);
        
        if (isDirectory(path)) {
            // delete directory contents along with the directory itself
            log.debug("Force deleting directory: {}", path);
            try (Stream<Path> stream = Files.walk(path)) {
                List<Path> entries = stream.sorted(Comparator.reverseOrder()).toList();
                for (Path p : entries) {
                    log.debug("Deleting file/directory: {}", p);
                    deleteFile(p);
                }
            }
        } else {
            log.debug("Deleting file: {}", path);
            deleteFile(path);
        }

        log.debug("Finished force deleting: {}", path);
    }

    @Override
    public List<Path> glob(Path baseDirectory, String globPattern) throws IOException {
        if (!Files.exists(baseDirectory)) {
            throw new IOException("Base directory does not exist: " + baseDirectory);
        }
        if (!Files.isDirectory(baseDirectory)) {
            throw new IOException("Base directory is not a directory: " + baseDirectory);
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);

        try (Stream<Path> stream = Files.find(baseDirectory, Integer.MAX_VALUE, (p, attrs) -> {
            Path relative = baseDirectory.relativize(p);
            // also try with a synthetic parent so that patterns like **/*.yaml match root-level files,
            // consistent with the behaviour of PathMatchingResourcePatternResolver
            return matcher.matches(relative) || matcher.matches(Path.of("_").resolve(relative));
        })) {
            return stream.toList();
        }
    }
}
