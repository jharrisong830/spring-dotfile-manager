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
    name = "list",
    description = "List all dotfiles in the configured repository",
    mixinStandardHelpOptions = true
)
@Slf4j
public class ListCommand implements Callable<Integer> {
    
    private final DotfileService dotfileService;

    public ListCommand(DotfileService dotfileService) {
        this.dotfileService = dotfileService;
    }

    @Override
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();
        
        if (markers.isEmpty()) {
            System.out.println("No dotfiles found in the configured repository.");
        } else {
            System.out.println("Dotfiles in configured repository:");
            for (DotfileMarkerModel marker : markers) {
                System.out.println("- " + marker);
            }
        }

        return 0;
    }
}
