package app.jhg.spring_dotfile_manager.commands;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "unlink",
    description = "Unlink all dotfiles in the configured repository",
    mixinStandardHelpOptions = true
)
@Slf4j
public class UnlinkCommand implements Callable<Integer> {
    
    private final DotfileService dotfileService;

    public UnlinkCommand(DotfileService dotfileService) {
        this.dotfileService = dotfileService;
    }

    @Override 
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();
        if (markers.isEmpty()) {
            log.info("No dotfiles found to unlink in the configured repository.");
        } else {
            for (DotfileMarkerModel marker : markers) {
                try {
                    dotfileService.unlinkDotfile(marker);
                } catch (Exception e) {
                    log.error("Error occurred while unlinking dotfile: {}", marker.location, e);
                }
            }
        }

        return 0;
    }
}
