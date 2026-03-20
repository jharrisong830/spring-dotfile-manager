package app.jhg.spring_dotfile_manager.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@CommandLine.Command(
    name = "sdfm",
    subcommands = {
        InitCommand.class,
        CommandLine.HelpCommand.class
    },
    mixinStandardHelpOptions = true
)
public class RootCommand implements Runnable {

    @Override
    public void run() {}
}
