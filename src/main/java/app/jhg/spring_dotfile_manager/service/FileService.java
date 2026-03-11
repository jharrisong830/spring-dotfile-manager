package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {
    public boolean exists(Path path);
    public boolean isDirectory(Path path);
    public boolean isSymbolicLink(Path path);
    public void createDirectories(Path path) throws IOException;
    public void writeFile(Path path, String content) throws IOException;
    public void overwriteFile(Path path, String content) throws IOException;
    public String readFile(Path path) throws IOException;
}
