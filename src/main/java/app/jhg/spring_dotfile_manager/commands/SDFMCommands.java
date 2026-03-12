package app.jhg.spring_dotfile_manager.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.ConfigService;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;

@Component
public class SDFMCommands {

    private final String defaultRepoPath;

    private final ConfigService configService;
    private final DotfileService dotfileService;

    public SDFMCommands(
        @Value("${spring-dotfile-manager.config.default-repo-path}") String defaultRepoPath,
        ConfigService configService,
        DotfileService dotfileService
    ) {
        this.defaultRepoPath = defaultRepoPath;
        this.configService = configService;
        this.dotfileService = dotfileService;
    }

    
    @Command(name = "init", description = "Initialize the configuration for Spring Dotfile Manager", exitStatusExceptionMapper = "exitStatusExceptionMapper")
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
            context.outputWriter().println("No dotfile repository path provided.");
            context.outputWriter().println("Default repository path is: '" + FormattingUtils.formatWithHomeDirectory(defaultRepoPath) + "'");
            context.outputWriter().println("Delete the file and re-run with a custom path, or run set-config to change the path later.");

            context.outputWriter().flush();
        }

        if (dotfileRepoPath.isEmpty()) {
            dotfileRepoPath = defaultRepoPath;
        }

        configService.initializeConfig(dotfileRepoPath);
        printConfig(context.outputWriter(), dotfileRepoPath);
    }

    @Command(name = "get-config", description = "Get the current dotfile repository configuration", exitStatusExceptionMapper = "exitStatusExceptionMapper")
    public void getConfig(CommandContext context) throws IOException {
        String dotfileRepoPath = configService.readConfig();
        printConfig(context.outputWriter(), dotfileRepoPath);
    }

    @Command(name = "set-config", description = "Set the dotfile repository path in the configuration", exitStatusExceptionMapper = "exitStatusExceptionMapper")
    public void setConfig(
        @Argument(
            index = 0,
            description = "New dotfile repository path"
        ) String dotfileRepoPath,
        CommandContext context
    ) throws Exception {
        dotfileRepoPath = dotfileRepoPath.trim();
        if (dotfileRepoPath.isEmpty()) {            
            throw new IllegalArgumentException("Dotfile repository path cannot be empty. Please provide a valid path.");
        }

        configService.updateConfig(dotfileRepoPath);
        printConfig(context.outputWriter(), dotfileRepoPath);
    }

    @Command(name = "list", description = "List all dotfiles in the configured repository", exitStatusExceptionMapper = "exitStatusExceptionMapper")
    public void list(CommandContext context) throws Exception {
        List<DotfileMarkerModel> markerModels = dotfileService.getAllDotfileMarkerModels();
        if (markerModels.isEmpty()) {
            context.outputWriter().println("No dotfiles found in the configured repository.");
        } else {
            context.outputWriter().println("Dotfiles in configured repository:");
            for (DotfileMarkerModel model : markerModels) {
                context.outputWriter().println("- " + model);
            }
        }
        context.outputWriter().flush();
    }


    private void printConfig(PrintWriter outputWriter, String dotfileRepoPath) {
        outputWriter.println("Configuration at: " + configService.getConfigFilePath());
        outputWriter.println("Using dotfile repository path: '" + FormattingUtils.formatWithHomeDirectory(dotfileRepoPath) + "'");
        outputWriter.flush();
    }
}
