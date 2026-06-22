package app.jhg.spring_dotfile_manager.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import app.jhg.spring_dotfile_manager.config.VersionProviderConfiguration;
import picocli.CommandLine;

public class RootCommandUnitTests {

    @Test
    public void testSubcommands_allExpectedSubcommandsRegistered() {
        // RootCommand references VersionProviderConfiguration by class, which has no no-arg
        // constructor; supply it manually since there's no Spring context in this unit test.
        CommandLine.IFactory factory = new CommandLine.IFactory() {
            @Override
            public <K> K create(Class<K> cls) throws Exception {
                if (cls == VersionProviderConfiguration.class) {
                    return cls.cast(new VersionProviderConfiguration("test-version"));
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
