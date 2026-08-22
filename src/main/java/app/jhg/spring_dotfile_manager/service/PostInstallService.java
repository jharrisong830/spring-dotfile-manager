package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;

public interface PostInstallService {

    /**
     * locates and runs all bash scripts using the pattern `post-install/\*\*\/\*\.sh`
     * @return list of post install results for each script run, indicating whether it was successful, and any command output details
     * @throws IOException if there are issues reading from the user's config file
     */
    List<PostInstallScriptResult> runPostInstallScripts() throws IOException;

    /**
     * locates all bash scripts using the pattern `post-install/\*\*\/\*\.sh`, without running them, sorted for deterministic execution order
     * @return list of discovered post-install script paths, or an empty list if post-install scripts are disabled or the current platform is Windows (not yet supported, since scripts require bash)
     * @throws IOException if there are issues reading from the user's config file or scanning the dotfile repository
     */
    List<Path> findPostInstallScripts() throws IOException;
}
