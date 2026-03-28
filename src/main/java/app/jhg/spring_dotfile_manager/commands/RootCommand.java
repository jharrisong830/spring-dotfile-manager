package app.jhg.spring_dotfile_manager.commands;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.config.DebugMixin;
import app.jhg.spring_dotfile_manager.config.DotfileRepoPathMixin;
import app.jhg.spring_dotfile_manager.config.VersionProviderConfiguration;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Mixin;

@Component
@Command(
    name = "sdfm",
    versionProvider = VersionProviderConfiguration.class,
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
public class RootCommand {

    @Mixin
    private DebugMixin debugMixin;

    @Mixin
    private DotfileRepoPathMixin dotfileRepoPathMixin;

    public RootCommand(DotfileRepoPathMixin dotfileRepoPathMixin) {
        this.dotfileRepoPathMixin = dotfileRepoPathMixin;
    }
}
