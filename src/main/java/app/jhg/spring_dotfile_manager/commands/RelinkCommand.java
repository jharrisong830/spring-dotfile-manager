package app.jhg.spring_dotfile_manager.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.config.KeyMixin;
import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import app.jhg.spring_dotfile_manager.service.PostInstallService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Component
@Command(
    name = "relink",
    description = "Relink all dotfiles in the configured repository",
    mixinStandardHelpOptions = true
)
@Slf4j
public class RelinkCommand implements Callable<Integer> {

    private final DotfileService dotfileService;
    private final PostInstallService postInstallService;
    private final BufferedReader stdinReader;

    @Mixin
    private KeyMixin keyMixin;

    public RelinkCommand(DotfileService dotfileService, PostInstallService postInstallService, BufferedReader stdinReader) {
        this.dotfileService = dotfileService;
        this.postInstallService = postInstallService;
        this.stdinReader = stdinReader;
    }

    @Override
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();
        int exitCode = 0;
        
        if (markers.isEmpty()) {
            log.info("No dotfiles found to relink in the configured repository.");
        } else {
            for (DotfileMarkerModel marker : markers) {
                try {
                    log.debug("Initial attempt to relink");
                    dotfileService.relinkDotfile(marker);
                } catch (FileAlreadyExistsException e) {
                    log.debug("FileAlreadyExistsException caught during relinking attempt");
                    Path targetPath = dotfileService.getTargetPathForCurrentSystem(marker);
                    log.info(e.getMessage());
                    log.info("Do you want to overwrite it with a symlink to {}? (only 'yes' will be accepted)", marker.sourceLocation);

                    String line = stdinReader.readLine();
                    String response = line != null ? line.trim() : "";

                    if (response.equalsIgnoreCase("yes")) {
                        log.debug("User confirmed overwrite");
                        try {
                            dotfileService.overwriteExistingDotfile(marker);
                            log.info("Overwrote existing file/directory at {} with symlink to {}", targetPath, marker.sourceLocation);
                        } catch (IOException overwriteException) {
                            log.error("Failed to overwrite {}: {}", targetPath, overwriteException.getMessage());
                            exitCode = 1;
                        }
                    } else {
                        log.info("Skipped relinking for {}", targetPath);
                    }
                }
            }
        }

        try {
            log.debug("Finding post-install scripts...");
            List<Path> postInstallScripts = postInstallService.findPostInstallScripts();

            if (postInstallScripts.isEmpty()) {
                log.debug("No post-install scripts found, or post-install scripts disabled.");
            } else {
                log.info("Found {} post-install script(s) to run:", postInstallScripts.size());
                for (Path script : postInstallScripts) {
                    log.info("  {}", script);
                }
                log.info("Do you want to run these post-install scripts? (only 'yes' will be accepted)");

                String line = stdinReader.readLine();
                String response = line != null ? line.trim() : "";

                if (response.equalsIgnoreCase("yes")) {
                    List<PostInstallScriptResult> postInstallScriptResults = postInstallService.runPostInstallScripts();

                    for (PostInstallScriptResult result : postInstallScriptResults) {
                        if (result.success()) {
                            log.info("Ran post-install script {}", result.script());
                            log.debug(result.message());
                        } else {
                            log.error("Post-install script {} failed: {}", result.script(), result.message());
                            exitCode = 1;
                        }
                    }
                } else {
                    log.info("Skipped running post-install scripts.");
                }
            }
        } catch (IOException e) {
            log.error("File I/O error while executing post-install scripts: {}", e.getMessage());
            exitCode = 1;
        }

        return exitCode;
    }
}
