package app.jhg.spring_dotfile_manager.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class DebugMixin {

    @CommandLine.Option(
        names = "--debug",
        defaultValue = "false",
        scope = CommandLine.ScopeType.INHERIT
    )
    public void configureDebugLogging(boolean debug) {
        if (debug) {
            ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
        }
    }
}
