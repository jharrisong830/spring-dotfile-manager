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

import org.junit.jupiter.api.Test;

public class VersionProviderConfigurationUnitTests {

    @Test
    public void testGetVersion_returnsFormattedVersionArray() {
        String testVersion = "1.2.3";
        VersionProviderConfiguration provider = new VersionProviderConfiguration(testVersion);

        String[] result = provider.getVersion();

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("spring-dotfile-manager (sdfm)", result[0]);
        assertEquals("1.2.3", result[1]);
    }

    @Test
    public void testGetVersion_withSnapshotVersion_returnsFormattedVersionArray() {
        String testVersion = "2.0.0-SNAPSHOT";
        VersionProviderConfiguration provider = new VersionProviderConfiguration(testVersion);

        String[] result = provider.getVersion();

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("spring-dotfile-manager (sdfm)", result[0]);
        assertEquals("2.0.0-SNAPSHOT", result[1]);
    }

    @Test
    public void testGetVersion_withEmptyVersion_returnsFormattedVersionArray() {
        String testVersion = "";
        VersionProviderConfiguration provider = new VersionProviderConfiguration(testVersion);

        String[] result = provider.getVersion();

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("spring-dotfile-manager (sdfm)", result[0]);
        assertEquals("", result[1]);
    }
}
