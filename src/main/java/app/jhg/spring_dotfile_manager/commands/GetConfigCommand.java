package app.jhg.spring_dotfile_manager.commands;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Component
@CommandLine.Command(
    name = "get-config",
    description = "Get the current dotfile repository configuration",
    mixinStandardHelpOptions = true
)
@Slf4j
public class GetConfigCommand implements Callable<Integer> {
    
    private final ConfigService configService;

    public GetConfigCommand(ConfigService configService) {
        this.configService = configService;
    }
    
    @Override
    public Integer call() throws Exception {
        String dotfileRepoPath = configService.readConfig();
        printConfig(dotfileRepoPath);
        return 0;
    }

    private void printConfig(String dotfileRepoPath) {
        log.info("Configuration at: {}", configService.getConfigFilePath());
        log.info("Using dotfile repository path: '{}'", FormattingUtils.formatWithHomeDirectory(dotfileRepoPath));
    }
}
