package app.jhg.spring_dotfile_manager.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Component
@Command(
    name = "sdfm",
    subcommands = {
        InitCommand.class,
        GetConfigCommand.class,
        SetConfigCommand.class,
        ListCommand.class,
        RelinkCommand.class,
        HelpCommand.class
    },
    mixinStandardHelpOptions = true
)
public class RootCommand implements Runnable {

    @Override
    public void run() {}
}
