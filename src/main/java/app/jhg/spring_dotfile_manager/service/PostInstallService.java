package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.util.List;

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;

public interface PostInstallService {
    public List<PostInstallScriptResult> runPostInstallScripts() throws IOException;
}
