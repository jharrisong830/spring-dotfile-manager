package app.jhg.spring_dotfile_manager.commands;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.config.DebugMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Mixin;

@Component
@Command(
    name = "sdfm",
    subcommands = {
        InitCommand.class,
        GetConfigCommand.class,
        SetConfigCommand.class,
        ListCommand.class,
        RelinkCommand.class,
        UnlinkCommand.class,
        HelpCommand.class
    },
    mixinStandardHelpOptions = true
)
public class RootCommand implements Runnable {

    @Mixin
    private DebugMixin debugMixin;

    @Override
    public void run() {}
}
