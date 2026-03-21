package app.jhg.spring_dotfile_manager.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class DebugMixinUnitTests {

    private Logger rootLogger;
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        originalLevel = rootLogger.getLevel();
    }

    @AfterEach
    void tearDown() {
        rootLogger.setLevel(originalLevel);
    }

    @Test
    public void testConfigureDebugLogging_true_setsDebugLevel() {
        new DebugMixin().configureDebugLogging(true);
        assertEquals(Level.DEBUG, rootLogger.getLevel());
    }

    @Test
    public void testConfigureDebugLogging_false_doesNotChangeLevel() {
        new DebugMixin().configureDebugLogging(false);
        assertEquals(originalLevel, rootLogger.getLevel());
    }
}
