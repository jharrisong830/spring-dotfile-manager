package app.jhg.spring_dotfile_manager.model;

import java.nio.file.Path;

public class PlatformOverrideModel {
    
    public final Platform platform;
    public final boolean shouldLink;
    public final Path location;

    private PlatformOverrideModel(Platform platform, boolean shouldLink, Path location) {
        this.platform = platform;
        this.shouldLink = shouldLink;
        this.location = location;
    }

    public static PlatformOverrideModel parsePlatformRawSubdocument(Platform platform, Object rawSubdocument) {
        return null;
    }
}
