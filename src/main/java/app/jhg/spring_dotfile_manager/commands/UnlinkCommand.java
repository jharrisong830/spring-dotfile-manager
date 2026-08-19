package app.jhg.spring_dotfile_manager.commands;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.config.KeyMixin;
import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Component
@Command(
    name = "unlink",
    description = "Unlink all dotfiles in the configured repository",
    mixinStandardHelpOptions = true
)
@Slf4j
public class UnlinkCommand implements Callable<Integer> {

    private final DotfileService dotfileService;

    @Mixin
    private KeyMixin keyMixin;

    public UnlinkCommand(DotfileService dotfileService) {
        this.dotfileService = dotfileService;
    }

    @Override 
    public Integer call() throws Exception {
        String key = keyMixin.getKey();
        List<DotfileMarkerModel> markers;
        
        if (key != null) {
            markers = dotfileService.getMarkersByKeyForCurrentSystem(key);
            if (markers.isEmpty()) {
                log.error("No dotfile found with key '{}' for the current platform.", key);
                return 1;
            }
            if (markers.size() > 1) {
                log.error("Multiple dotfiles found with key '{}': {}. Refusing to unlink an ambiguous key.", 
                    key,
                    markers.stream().map(m -> m.sourceLocation).toList()
                );
                return 1;
            }
        } else {
            markers = dotfileService.getAllDotfileMarkerModels();
        }
        
        int exitCode = 0;

        if (markers.isEmpty()) {
            log.info("No dotfiles found to unlink in the configured repository.");
        } else {
            for (DotfileMarkerModel marker : markers) {
                try {
                    dotfileService.unlinkDotfile(marker);
                } catch (IOException e) {
                    log.error("Error occurred while unlinking dotfile: {} ({})", marker.location, e.getMessage());
                    exitCode = 1;
                }
            }
        }

        return exitCode;
    }
}
