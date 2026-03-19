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

    /**
     * relinks all dotfiles
     * @throws IOException if there is a regular file or directory where a link is supposed to be made, or if there is any I/O error
     */
    public void relinkDotfiles() throws IOException;

    /**
     * relinks an individual marker file
     * @param marker the dotfile marker representing the dotfile to be linked
     * @throws IOException if there is a regular file or directory where a link is supposed to be made, or if there is any I/O error
     */
    public void relinkDotfile(DotfileMarkerModel marker) throws IOException;
}
