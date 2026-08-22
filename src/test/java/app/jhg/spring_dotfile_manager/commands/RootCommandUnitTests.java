package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.util.Set;

import org.junit.jupiter.api.Test;

import app.jhg.spring_dotfile_manager.config.VersionProviderConfiguration;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import app.jhg.spring_dotfile_manager.service.PostInstallService;
import picocli.CommandLine;

public class RootCommandUnitTests {

    @Test
    public void testSubcommands_allExpectedSubcommandsRegistered() {
        // RootCommand references VersionProviderConfiguration by class, which has no no-arg
        // constructor; supply it manually since there's no Spring context in this unit test.
        // RelinkCommand/UnlinkCommand are constructor-injected (no no-arg constructor either) and
        // now carry a @Mixin field, which picocli resolves eagerly at model-build time -- supply
        // them manually too, with mocked collaborators, since nothing about their behavior is
        // exercised here.
        CommandLine.IFactory factory = new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                if (cls == VersionProviderConfiguration.class) {
                    return cls.cast(new VersionProviderConfiguration("test-version"));
                }
                if (cls == RelinkCommand.class) {
                    return cls.cast(new RelinkCommand(mock(DotfileService.class), mock(PostInstallService.class), mock(BufferedReader.class)));
                }
                if (cls == UnlinkCommand.class) {
                    return cls.cast(new UnlinkCommand(mock(DotfileService.class)));
                }
                return CommandLine.defaultFactory().create(cls);
            }
        };
        CommandLine cmd = new CommandLine(new RootCommand(), factory);

        assertEquals(
            Set.of("init", "get-config", "set-config", "list", "relink", "unlink", "help"),
            cmd.getSubcommands().keySet()
        );
    }
}
