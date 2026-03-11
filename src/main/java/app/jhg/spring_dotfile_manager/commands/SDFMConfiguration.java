package app.jhg.spring_dotfile_manager.commands;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;

@Configuration
public class SDFMConfiguration {

    @Bean
    public ExitStatusExceptionMapper exitStatusExceptionMapper() {
        return e -> {
            if (e instanceof FileAlreadyExistsException) {
                return new ExitStatus(1, "A file operation couldn't be completed because the specified file already exists: " + e.getMessage());
            } else if (e instanceof IOException) {
                return new ExitStatus(1, "An I/O error occurred during a file operation: " + e.getMessage());
            }
            return new ExitStatus(1, "Unknown exception: " + e.getMessage());
        };
    }
}
