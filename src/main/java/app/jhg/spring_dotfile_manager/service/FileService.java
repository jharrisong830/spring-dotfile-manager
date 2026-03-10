package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;

public interface FileService {
    public boolean exists(Path path);
    public boolean isDirectory(Path path);
    public boolean isSymbolicLink(Path path);
    public void writeFile(Path path, String content) throws FileExistsException, IOException;

    // TODO
    // public String readFile(Path path);
}
