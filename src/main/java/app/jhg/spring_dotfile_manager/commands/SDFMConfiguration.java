package app.jhg.spring_dotfile_manager.commands;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.exit.ExitStatusExceptionMapper;

@Configuration
public class SDFMConfiguration {

    @Bean
    public BufferedReader stdinReader() {
        return new BufferedReader(new InputStreamReader(System.in));
    }

    @Bean
    public ExitStatusExceptionMapper exitStatusExceptionMapper() {
        return e -> {
            Throwable cause = e instanceof InvocationTargetException ite
                ? (ite.getCause() != null ? ite.getCause() : ite)
                : e;

            if (cause instanceof FileAlreadyExistsException) { // check first bc subclass of IOException
                return new ExitStatus(1, "A file operation couldn't be completed because the specified file already exists: " + Objects.toString(cause.getMessage(), "(no message)"));
            } else if (cause instanceof NoSuchFileException || cause instanceof FileNotFoundException) {
                return new ExitStatus(1, "A file operation couldn't be completed because the specified file was not found: " + Objects.toString(cause.getMessage(), "(no message)"));
            } else if (cause instanceof IOException) {
                return new ExitStatus(1, "An I/O error occurred during a file operation: " + Objects.toString(cause.getMessage(), "(no message)"));
            } else if (cause instanceof IllegalArgumentException) {
                return new ExitStatus(1, "Invalid argument: " + Objects.toString(cause.getMessage(), "(no message)"));
            }
            return new ExitStatus(1, "Unknown exception: " + Objects.toString(cause.getMessage(), "(no message)"));
        };
    }
}
