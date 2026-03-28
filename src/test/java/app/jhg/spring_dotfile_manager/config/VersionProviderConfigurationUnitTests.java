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
