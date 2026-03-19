package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;

public interface DotfileService {

    /**
     * gets a list of all paths to dotfile marker files in the configured dotfile repository
     * @return a list of paths to dotfile marker files
     */
    public List<Path> getAllDotfileMarkerPaths() throws IOException;

    /**
     * gets a list of all dotfile markers in the configured dotfile repository
     * @return a list of dotfile marker models
     * @throws IOException if there is an error reading the dotfile marker files
     */
    public List<DotfileMarkerModel> getAllDotfileMarkerModels() throws IOException;

    /**
     * gets the dotfile markers objects specified by the marker file at the given path
     * @param path the path to the dotfile marker file
     * @return the dotfile marker models
     * @throws IOException if there is an error reading the dotfile marker file
     */
    public List<DotfileMarkerModel> getDotfileMarkerModelsByPath(Path path) throws IOException;


    public void relinkDotfiles() throws IOException;
}
