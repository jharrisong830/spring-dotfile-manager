package app.jhg.spring_dotfile_manager.commands;

import java.io.BufferedReader;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
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

    @Option(
        names = "--allow-post-install-scripts",
        arity = "1",
        description = "Whether to allow the execution of post-install scripts (true or false)"
    )
    private Boolean allowPostInstallScripts;

    private final ConfigService configService;
    private final BufferedReader stdinReader;

    public SetConfigCommand(ConfigService configService, BufferedReader stdinReader) {
        this.configService = configService;
        this.stdinReader = stdinReader;
    }

    @Override
    public Integer call() throws Exception {
        dotfileRepoPath = dotfileRepoPath.trim();
        boolean pathProvided = !dotfileRepoPath.isEmpty();
        boolean allowPostInstallProvided = allowPostInstallScripts != null;

        if (!pathProvided) {
            String currentConfig = configService.readDotfileRepoPath();
            log.info("No dotfile repository path provided.");
            log.info("Enter desired path, or <Enter> to keep current configuration ({})", currentConfig);

            String line = stdinReader.readLine();
            String customPath = line != null ? line.trim() : "";

            if (!customPath.isEmpty()) {
                dotfileRepoPath = customPath;
                pathProvided = true;
            } else {
                log.info("Keeping current configuration: {}", currentConfig);
                dotfileRepoPath = currentConfig;
            }
        }

        if (!allowPostInstallProvided) {
            allowPostInstallScripts = configService.readAllowPostInstallScripts();
            log.debug("Using existing preference for post-install scripts: {}", allowPostInstallScripts);
        }

        if (pathProvided || allowPostInstallProvided) {
            configService.updateConfig(dotfileRepoPath, allowPostInstallScripts);
        }
        configService.printConfig();
        return 0;
    }
}
