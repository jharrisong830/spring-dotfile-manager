package app.jhg.spring_dotfile_manager.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;

@Configuration
public class SDFMConfiguration {

    @Bean
    public ExitStatusExceptionMapper exitStatusExceptionMapper() {
        return e -> {
            Throwable cause = e instanceof InvocationTargetException ? e.getCause() : e;

            if (cause instanceof FileAlreadyExistsException) { // check first bc subclass of IOException
                return new ExitStatus(1, "A file operation couldn't be completed because the specified file already exists: " + cause.getMessage());
            } else if (cause instanceof NoSuchFileException || cause instanceof FileNotFoundException) {
                return new ExitStatus(1, "A file operation couldn't be completed because the specified file was not found: " + cause.getMessage());
            } else if (cause instanceof IOException) {
                return new ExitStatus(1, "An I/O error occurred during a file operation: " + cause.getMessage());
            } else if (cause instanceof IllegalArgumentException) {
                return new ExitStatus(1, "Invalid argument: " + cause.getMessage());
            }
            return new ExitStatus(1, "Unknown exception: " + cause.getMessage());
        };
    }
}
