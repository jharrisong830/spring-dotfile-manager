package app.jhg.spring_dotfile_manager.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;

import app.jhg.spring_dotfile_manager.commands.RootCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;

@Configuration
@Slf4j
public class SDFMConfiguration {

    @Bean
    public BufferedReader stdinReader() {
        return new BufferedReader(new InputStreamReader(System.in));
    }

    @Bean
    public CommandLine.IExecutionExceptionHandler executionExceptionHandler() {
        return (ex, commandLine, parseResult) -> {
            if (ex instanceof FileAlreadyExistsException) {
                log.error("A file operation couldn't be completed because the specified file already exists: {}", ex.getMessage());
                log.debug("{}", ex);
            } else if (ex instanceof NoSuchFileException || ex instanceof FileNotFoundException) {
                log.error("A file operation couldn't be completed because the specified file was not found: {}", ex.getMessage());
                log.debug("{}", ex);
            } else if (ex instanceof IOException) {
                log.error("An I/O error occurred during a file operation: {}", ex.getMessage());
                log.debug("{}", ex);
            } else if (ex instanceof IllegalArgumentException) {
                log.error("Invalid argument: {}", ex.getMessage());
                log.debug("{}", ex);
            } else {
                log.error("Unknown exception: {}", ex.getMessage());
                log.debug("{}", ex);
            }

            return 1;
        };
    }

    @Bean
    public CommandLine commandLine(
        RootCommand rootCommand,
        ApplicationContext ctx,
        IExecutionExceptionHandler executionExceptionHandler
    ) {
        CommandLine.IFactory factory = new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                try {
                    return ctx.getBean(cls);
                } catch (Exception e) {
                    return CommandLine.defaultFactory().create(cls);
                }
            }
        };

        return new CommandLine(rootCommand, factory).setExecutionExceptionHandler(executionExceptionHandler);
    }
}
