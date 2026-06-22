package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;
import app.jhg.spring_dotfile_manager.model.SubprocessResult;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostInstallServiceImpl implements PostInstallService {

    private final String postInstallGlobPattern;

    private final ConfigService configService;
    private final FileService fileService;
    private final SubprocessService subprocessService;

    public PostInstallServiceImpl(
        @Value("${spring-dotfile-manager.post-install-glob-pattern}") String postInstallGlobPattern,
        ConfigService configService,
        FileService fileService,
        SubprocessService subprocessService
    ) {
        this.postInstallGlobPattern = postInstallGlobPattern;
        this.configService = configService;
        this.fileService = fileService;
        this.subprocessService = subprocessService;
    }

    @Override
    public List<PostInstallScriptResult> runPostInstallScripts() throws IOException {
        if (!configService.readAllowPostInstallScripts()) {
            // return an empty list immediately if post-install scripts are not allowed
            log.debug("Post-install scripts disabled");
            return List.of();
        }

        log.debug("Finding {} in {}", postInstallGlobPattern, configService.readDotfileRepoPath());
        Path dotfileRepoPath = Path.of(FormattingUtils.formatWithHomeDirectory(configService.readDotfileRepoPath()));
        List<Path> allPostInstallScripts = new ArrayList<>(fileService.glob(dotfileRepoPath, postInstallGlobPattern));
        allPostInstallScripts.sort(Comparator.naturalOrder());

        List<PostInstallScriptResult> results = new ArrayList<>();
        for (Path scriptPath : allPostInstallScripts) {
            log.debug("Starting execution for {}", scriptPath);
            List<String> cmd = List.of("bash", scriptPath.toString());
            try {
                SubprocessResult res = subprocessService.executeCommand(scriptPath.getParent(), cmd);
                results.add(new PostInstallScriptResult(res.exitCode() == 0, res.output(), scriptPath));
            } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                results.add(new PostInstallScriptResult(false, "Failed to run post-install script: " + e.getMessage(), scriptPath));
            }
            log.debug("Finished execution for {}", scriptPath);
        }

        return results;
    }
}
