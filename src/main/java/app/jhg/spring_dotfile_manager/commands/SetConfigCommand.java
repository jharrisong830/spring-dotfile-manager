package app.jhg.spring_dotfile_manager.commands;

import java.io.BufferedReader;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(
    name = "set-config",
    description = "Set the dotfile repository configuration",
    mixinStandardHelpOptions = true
)
@Slf4j
public class SetConfigCommand implements Callable<Integer>{
    
    @Parameters(
        index = "0",
        description = "Path to your dotfile repository",
        defaultValue = ""
    )
    private String dotfileRepoPath;

    private final ConfigService configService;
    private final BufferedReader stdinReader;

    public SetConfigCommand(ConfigService configService, BufferedReader stdinReader) {
        this.configService = configService;
        this.stdinReader = stdinReader;
    }

    public Integer call() throws Exception {
        dotfileRepoPath = dotfileRepoPath.trim();
        if (dotfileRepoPath.isEmpty()) {
            log.info("No dotfile repository path provided.");
            log.info("Enter desired path, or <Enter> to keep current configuration ({})", configService.readConfig());

            String line = stdinReader.readLine();
            String customPath = line != null ? line.trim() : "";

            if (!customPath.isEmpty()) {
                dotfileRepoPath = customPath;
            } else {
                throw new IllegalArgumentException("Dotfile repository path cannot be empty. Please provide a valid path.");
            }
        }

        configService.updateConfig(dotfileRepoPath);
        configService.printConfig();
        return 0;
    }
}
