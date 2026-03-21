package app.jhg.spring_dotfile_manager.commands;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.util.concurrent.Callable;

@Component
@CommandLine.Command(
    name = "init",
    description = "Initialize the configuration for Spring Dotfile Manager",
    mixinStandardHelpOptions = true
)
@Slf4j
public class InitCommand implements Callable<Integer> {

    @CommandLine.Parameters(
        index = "0",
        description = "Path to your dotfile repository",
        defaultValue = ""
    )
    private String dotfileRepoPath;

    private final String defaultDotfileRepoPath;

    private final ConfigService configService;
    private final BufferedReader stdinReader;

    public InitCommand(
        @Value("${spring-dotfile-manager.config.default-repo-path}") String defaultDotfileRepoPath,
        ConfigService configService,
        BufferedReader stdinReader
    ) {
        this.defaultDotfileRepoPath = defaultDotfileRepoPath;
        this.configService = configService;
        this.stdinReader = stdinReader;
    }

    @Override
    public Integer call() throws Exception {
        dotfileRepoPath = dotfileRepoPath.trim();
        if (dotfileRepoPath.isEmpty()) {
            log.info("No dotfile repository path provided.");
            log.info("Enter desired path, or <Enter> to accept default ({})", FormattingUtils.formatWithHomeDirectory(defaultDotfileRepoPath));

            String line = stdinReader.readLine();
            String customPath = line != null ? line.trim() : "";

            if (!customPath.isEmpty()) {
                dotfileRepoPath = customPath;
            } else {
                dotfileRepoPath = defaultDotfileRepoPath;
            }
        }

        configService.initializeConfig(dotfileRepoPath);
        printConfig(dotfileRepoPath);

        return 0;
    }


    private void printConfig(String dotfileRepoPath) {
        log.info("Configuration at: {}", configService.getConfigFilePath());
        log.info("Using dotfile repository path: '{}'", FormattingUtils.formatWithHomeDirectory(dotfileRepoPath));
    }
}
