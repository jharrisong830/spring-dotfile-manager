package app.jhg.spring_dotfile_manager.commands;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class PicocliRunner implements ApplicationRunner, ExitCodeGenerator {

    private int exitCode;

    private final CommandLine commandLine;

    public PicocliRunner(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public void run(ApplicationArguments args) {
        exitCode = commandLine.execute(args.getSourceArgs());
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
