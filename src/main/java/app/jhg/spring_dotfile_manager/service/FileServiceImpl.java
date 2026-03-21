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
    public void createSymlink(Path linkPath, Path source) throws IOException {
        Files.createSymbolicLink(linkPath, source);
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
    public void deleteFile(Path path) throws IOException {
        Files.delete(path);
    }

    @Override
    public void forceDelete(Path path) throws IOException {
        if (isDirectory(path)) {
            // delete directory contents along with the directory itself
            try (Stream<Path> stream = Files.walk(path)) {
                List<Path> entries = stream.sorted(Comparator.reverseOrder()).toList();
                for (Path p : entries) {
                    deleteFile(p);
                }
            }
        } else {
            deleteFile(path);
        }
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
