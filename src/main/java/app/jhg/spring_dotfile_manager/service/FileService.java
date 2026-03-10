package app.jhg.spring_dotfile_manager.service;

import java.nio.file.Path;

public interface FileService {
    public boolean exists(Path path);

    // TODO
    // public boolean isDirectory(Path path);
    // public boolean isSymbolicLink(Path path);
    // public void writeFile(Path path, String content);
    // public String readFile(Path path);
}
