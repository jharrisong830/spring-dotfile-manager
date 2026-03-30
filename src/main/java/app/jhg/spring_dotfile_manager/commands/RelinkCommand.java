package app.jhg.spring_dotfile_manager.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "relink",
    description = "Relink all dotfiles in the configured repository",
    mixinStandardHelpOptions = true
)
@Slf4j
public class RelinkCommand implements Callable<Integer> {
    
    private final DotfileService dotfileService;
    private final BufferedReader stdinReader;

    public RelinkCommand(DotfileService dotfileService, BufferedReader stdinReader) {
        this.dotfileService = dotfileService;
        this.stdinReader = stdinReader;
    }

    @Override
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();
        
        if (markers.isEmpty()) {
            log.info("No dotfiles found to relink in the configured repository.");
        } else {
            for (DotfileMarkerModel marker : markers) {
                try {
                    log.debug("Initial attempt to relink");
                    dotfileService.relinkDotfile(marker);
                } catch (FileAlreadyExistsException e) {
                    log.debug("FileAlreadyExistsException caught during relinking attempt");
                    log.info(e.getMessage());
                    log.info("Do you want to overwrite it with a symlink to {}? (only 'yes' will be accepted)", marker.sourceLocation);

                    String line = stdinReader.readLine();
                    String response = line != null ? line.trim() : "";

                    if (response.equalsIgnoreCase("yes")) {
                        log.debug("User confirmed overwrite");
                        try {
                            dotfileService.overwriteExistingDotfile(marker);
                            log.info("Overwrote existing file/directory with symlink to {}", marker.sourceLocation);
                        } catch (IOException overwriteException) {
                            log.error("Failed to overwrite {}: {}", marker.location, overwriteException.getMessage());
                        }
                    } else {
                        log.info("Skipped relinking for {}", marker.location);
                    }
                }
            }
        }

        return 0;
    }
}
