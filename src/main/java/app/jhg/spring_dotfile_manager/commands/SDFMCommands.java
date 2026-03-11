package app.jhg.spring_dotfile_manager.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import app.jhg.spring_dotfile_manager.service.FormatterService;

@Component
public class SDFMCommands {

    private final String defaultRepoPath;

    private final ConfigService configService;
    private final FormatterService formatterService;

    public SDFMCommands(
        @Value("${spring-dotfile-manager.config.default-repo-path}") String defaultRepoPath,
        ConfigService configService,
        FormatterService formatterService
    ) {
        this.defaultRepoPath = defaultRepoPath;
        this.configService = configService;
        this.formatterService = formatterService;
    }

    
    @Command(name = "init", description = "Initialize the configuration for Spring Dotfile Manager")
    public void init(
        @Argument(
            index = 0, 
            description = "Path to your dotfile repository", 
            defaultValue = ""
        ) String dotfileRepoPath,
        CommandContext context
    ) throws Exception {
        dotfileRepoPath = dotfileRepoPath.trim();
        if (dotfileRepoPath.isEmpty()) {
            dotfileRepoPath = context.inputReader().readInput("Enter path to your dotfile repository (none = accept default)").trim();
        }

        if (dotfileRepoPath.isEmpty()) {
            dotfileRepoPath = defaultRepoPath;
        }

        configService.initializeConfig(dotfileRepoPath);

        context.outputWriter().println("Wrote configuration file to " + configService.getConfigFilePath());
        context.outputWriter().println("Using dotfile repository path: '" + formatterService.formatWithHomeDirectory(dotfileRepoPath) + "'");
        context.outputWriter().flush();
    }

    @Command(name = "get-config", description = "Get the current dotfile repository configuration")
    public void getConfig(CommandContext context) throws IOException {
        String dotfileRepoPath = configService.readConfig();

        context.outputWriter().println("Using dotfile repository path: '" + formatterService.formatWithHomeDirectory(dotfileRepoPath) + "'");
        context.outputWriter().flush();
    }
}
