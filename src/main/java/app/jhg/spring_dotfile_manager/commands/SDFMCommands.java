package app.jhg.spring_dotfile_manager.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.annotation.Argument;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;
import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;
import app.jhg.spring_dotfile_manager.service.ConfigService;

@Component
public class SDFMCommands {

    @Bean
    public ExitStatusExceptionMapper exitStatusExceptionMapper() {
        return e -> {
            if (e instanceof FileExistsException) {
                return new ExitStatus(1, "A file operation couldn't be completed because the specified file already exists: " + e.getMessage());
            } else if (e instanceof IOException) {
                return new ExitStatus(1, "An I/O error occurred during a file operation: " + e.getMessage());
            }
            return new ExitStatus(1, "Unknown exception: " + e.getMessage());
        };
    }

    private final String defaultRepoPath;

    private final ConfigService configService;

    public SDFMCommands(
        @Value("${spring-dotfile-manager.config.default-repo-path}") String defaultRepoPath,
        ConfigService configService
    ) {
        this.defaultRepoPath = defaultRepoPath;
        this.configService = configService;
    }

    
    @Command(name = "init", description = "Initialize the configuration for Spring Dotfile Manager")
    public void init(
        @Argument(
            index = 0, 
            description = "Path to your dotfile repository", 
            defaultValue = ""
        ) String dotfileRepoPath,
        CommandContext context
    ) throws Exception, FileExistsException, IOException {
        dotfileRepoPath = dotfileRepoPath.trim();
        if (dotfileRepoPath.trim().isEmpty()) {
            dotfileRepoPath = context.inputReader().readInput("Enter path to your dotfile repository (none = accept default)");
        }

        dotfileRepoPath = dotfileRepoPath.trim();
        if (dotfileRepoPath.isEmpty()) {
            dotfileRepoPath = defaultRepoPath;
        }

        configService.initializeConfig(dotfileRepoPath);
    }
}
