package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import app.jhg.spring_dotfile_manager.service.SubprocessServiceImpl.SubprocessResult;

public interface SubprocessService {
    
    /**
     * executes a command in the given working directory
     * @param cwd current working directory to operate in
     * @param args command as a list of strings
     * @return exit code
     */
    public SubprocessResult executeCommand(Path cwd, List<String> args) throws IOException, InterruptedException, ExecutionException, TimeoutException;
}
