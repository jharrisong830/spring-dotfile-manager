// spring-dotfile-manager
// Copyright (C) 2026  John Graham

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

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
