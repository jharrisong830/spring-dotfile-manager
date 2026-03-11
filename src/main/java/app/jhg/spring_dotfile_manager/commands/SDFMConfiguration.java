package app.jhg.spring_dotfile_manager.commands;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;

import app.jhg.spring_dotfile_manager.exception.FileExistsException;

@Configuration
public class SDFMConfiguration {

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
}
