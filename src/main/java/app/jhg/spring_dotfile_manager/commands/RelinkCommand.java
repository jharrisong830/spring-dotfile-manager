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
    name = "relink",
    description = "Relink all dotfiles in the configured repository",
    mixinStandardHelpOptions = true
)
@Slf4j
public class RelinkCommand implements Callable<Integer> {
    
    private final DotfileService dotfileService;

    public RelinkCommand(DotfileService dotfileService) {
        this.dotfileService = dotfileService;
    }

    @Override
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();
        
        if (markers.isEmpty()) {
            log.info("No dotfiles found to relink in the configured repository.");
        } else {
            for (DotfileMarkerModel marker : markers) {
                dotfileService.relinkDotfile(marker);
            }
        }

        return 0;
    }
}
