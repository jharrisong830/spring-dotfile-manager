package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;

public interface ConfigService {
    public void initializeConfig(String dotfileRepoPath) throws FileExistsException, IOException;
}
