package app.jhg.spring_dotfile_manager.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class DotfileMarkerModel {

    public static class PlatformOverrideModel {
        
        public final Platform platform;
        public final boolean shouldLink;
        public final Path location;

        private PlatformOverrideModel(Platform platform, boolean shouldLink, Path location) {
            this.platform = platform;
            this.shouldLink = shouldLink;
            this.location = location;
        }

        public static PlatformOverrideModel parsePlatformRawSubdocument(Platform platform, Object rawSubdocument) {
            if (!(rawSubdocument instanceof Map<?, ?> rawSubdocumentMap)) {
                throw new IllegalArgumentException("Invalid marker file contents: expected platform override subdocument to be a mapping");
            }

            if (!rawSubdocumentMap.containsKey("shouldLink")) {
                throw new IllegalArgumentException("Invalid marker file contents: platform override subdocument must contain 'shouldLink' key");
            }
            if (!(rawSubdocumentMap.get("shouldLink") instanceof Boolean shouldLink)) {
                throw new IllegalArgumentException("Invalid marker file contents: 'shouldLink' value in platform override subdocument must be a boolean");
            }
            if (shouldLink && !rawSubdocumentMap.containsKey("location")) {
                throw new IllegalArgumentException("Invalid marker file contents: platform override subdocument must contain 'location' key when 'shouldLink' is true");
            }

            Object rawLocation = rawSubdocumentMap.get("location");
            Path locationPath = null;
            if (shouldLink) {
                if (!(rawLocation instanceof String location) || location.isBlank()) {
                    throw new IllegalArgumentException("Invalid marker file contents: 'location' value in platform override subdocument must be a non-blank string");
                }
                locationPath = Path.of(location);
            }

            return new PlatformOverrideModel(platform, shouldLink, locationPath);
        }
    }
    
    public final String name;
    public final Path location;
    public final Path markerFilePath;

    public final PlatformOverrideModel linuxOverride;
    public final PlatformOverrideModel win32Override;
    public final PlatformOverrideModel darwinOverride;

    private DotfileMarkerModel(
        String name,
        Path location,
        Path markerFilePath,
        PlatformOverrideModel linuxOverride,
        PlatformOverrideModel win32Override,
        PlatformOverrideModel darwinOverride
    ) {
        this.name = name;
        this.location = location;
        this.markerFilePath = markerFilePath;
        this.linuxOverride = linuxOverride;
        this.win32Override = win32Override;
        this.darwinOverride = darwinOverride;
    }


    public static List<DotfileMarkerModel> fromMarkerFileContents(Path markerFilePath, String markerFileContents) {
        Yaml yaml = new Yaml();
        Iterable<Object> markerFileRawDocuments;

        try {
            markerFileRawDocuments = yaml.loadAll(markerFileContents);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid marker file contents: expected a YAML sequence of mappings", e);
        }

        List<DotfileMarkerModel> markerModels = new ArrayList<>();
        for (Object rawDocument : markerFileRawDocuments) {
            if (rawDocument != null) { // skip empty documents
                markerModels.add(parseRawDocumentObject(markerFilePath, rawDocument));
            }
        }
        return markerModels;
    }

    private static DotfileMarkerModel parseRawDocumentObject(Path markerFilePath, Object rawDocument) {
        if (!(rawDocument instanceof Map<?, ?> rawDocumentMap)) {
            throw new IllegalArgumentException("Invalid marker file contents: expected a YAML sequence of mappings");
        }

        if (!rawDocumentMap.containsKey("name") || !rawDocumentMap.containsKey("location")) {
            throw new IllegalArgumentException("Invalid marker file contents: each document must contain 'name' and 'location' keys");
        }

        Object rawName = rawDocumentMap.get("name");
        Object rawLocation = rawDocumentMap.get("location");

        if (!(rawName instanceof String name) || name.isBlank()) {
            throw new IllegalArgumentException("Invalid marker file contents: 'name' value must be a non-blank string");
        }
        if (!(rawLocation instanceof String location) || location.isBlank()) {
            throw new IllegalArgumentException("Invalid marker file contents: 'location' value must be a non-blank string");
        }

        PlatformOverrideModel linuxOverride = null;
        PlatformOverrideModel win32Override = null;
        PlatformOverrideModel darwinOverride = null;

        if (rawDocumentMap.containsKey("linux")) {
            linuxOverride = PlatformOverrideModel.parsePlatformRawSubdocument(Platform.LINUX, rawDocumentMap.get("linux"));
        }

        if (rawDocumentMap.containsKey("win32")) {
            win32Override = PlatformOverrideModel.parsePlatformRawSubdocument(Platform.WIN32, rawDocumentMap.get("win32"));
        }

        if (rawDocumentMap.containsKey("darwin")) {
            darwinOverride = PlatformOverrideModel.parsePlatformRawSubdocument(Platform.DARWIN, rawDocumentMap.get("darwin"));
        }

        return new DotfileMarkerModel(name, Path.of(location), markerFilePath, linuxOverride, win32Override, darwinOverride);
    }
}
