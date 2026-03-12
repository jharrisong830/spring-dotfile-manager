package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DotfileService {

    /**
     * gets a list of all paths to dotfile marker files in the configured dotfile repository
     * @return a list of paths to dotfile marker files
     */
    public List<Path> getAllDotfileMarkerPaths() throws IOException;
}
