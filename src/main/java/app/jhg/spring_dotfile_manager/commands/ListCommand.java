package app.jhg.spring_dotfile_manager.commands;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "list", description = "List all dotfiles in the configured repository that would be linked on your current system", mixinStandardHelpOptions = true)
@Slf4j
public class ListCommand implements Callable<Integer> {

    @Option(names = { "-a",
            "--all" }, description = "Include all dotfiles and their detailed configuration, regardless of platform", defaultValue = "false")
    private boolean includeAll;

    private final DotfileService dotfileService;

    public ListCommand(DotfileService dotfileService) {
        this.dotfileService = dotfileService;
    }

    @Override
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();

        if (markers.isEmpty()) {
            log.info("No dotfiles found in the configured repository.");
            log.info("Please add some dotfiles to your repository and try again.");
            return 1;
        }

        if (!includeAll) {
            markers = markers.stream()
                    .filter(m -> dotfileService.getTargetPathForCurrentSystem(m) != null)
                    .toList();
        }

        if (markers.isEmpty()) {
            log.info("No dotfiles found in the configured repository for the current system.");
            log.info("Use --all option to see all dotfiles regardless of platform.");
            return 1;
        } else {
            log.info("Dotfiles in configured repository:");
            if (includeAll) {
                log.info("(Showing all dotfiles regardless of platform)");
            }
            for (DotfileMarkerModel marker : markers) {
                log.info("- {}", marker);
            }
        }

        return 0;
    }
}
