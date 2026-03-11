package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;

public interface ConfigService {
    public void initializeConfig(String dotfileRepoPath) throws IOException;
    public String readConfig() throws IOException;
    public void updateConfig(String newDotfileRepoPath) throws IOException;
}
