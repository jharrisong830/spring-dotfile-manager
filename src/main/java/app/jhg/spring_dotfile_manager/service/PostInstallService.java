package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.util.List;

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;

public interface PostInstallService {
    
    /**
     * locates and runs all bash scripts using the pattern `post-install/\*\*\/\*\.sh`
     * @return list of post install results for each script run, indicating whether it was successful, and any command output details
     * @throws IOException if there are issues reading from the user's config file
     */
    public List<PostInstallScriptResult> runPostInstallScripts() throws IOException;
}
