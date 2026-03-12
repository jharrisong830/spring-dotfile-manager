package app.jhg.spring_dotfile_manager.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import app.jhg.spring_dotfile_manager.util.FormattingUtils;

public class DotfileMarkerModel {

    public static class PlatformOverrideModel {

        public final boolean shouldLink;
        public final Path location;

        private PlatformOverrideModel(boolean shouldLink, Path location) {
            this.shouldLink = shouldLink;
            this.location = location;
        }

        @Override
        public String toString() {
            return "PlatformOverrideModel{shouldLink=" + shouldLink + ", location=" + location + "}";
        }

        public static PlatformOverrideModel parsePlatformRawSubdocument(String filename, Object rawSubdocument) {
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
            Path locationPath = null; // will always be null if shouldLink is false
            if (shouldLink) {
                if (!(rawLocation instanceof String location) || location.isBlank()) {
                    throw new IllegalArgumentException("Invalid marker file contents: 'location' value in platform override subdocument must be a non-blank string");
                }

                location = FormattingUtils.formatWithName(location, filename); // resolve {NAME}
                location = FormattingUtils.formatWithHomeDirectory(location);
                locationPath = Path.of(location);
            }

            return new PlatformOverrideModel(shouldLink, locationPath);
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

    @Override
    public String toString() {
        return "DotfileMarkerModel{name='" + name + "', location=" + location + ", markerFilePath=" + markerFilePath + ", linuxOverride=" + linuxOverride + ", win32Override=" + win32Override + ", darwinOverride=" + darwinOverride + "}";
    }


    public static List<DotfileMarkerModel> fromMarkerFileContents(Path markerFilePath, String markerFileContents) {
        Yaml yaml = new Yaml();
        Iterable<Object> markerFileRawDocuments;

        try {
            markerFileRawDocuments = yaml.loadAll(markerFileContents);
        } catch (YAMLException e) {
            throw new IllegalArgumentException("Invalid marker file contents: could not parse YAML", e);
        }

        List<DotfileMarkerModel> markerModels = new ArrayList<>();
        for (Object rawDocument : markerFileRawDocuments) {
            if (rawDocument != null) { // skip empty documents
                markerModels.add(parseRawDocument(markerFilePath, rawDocument));
            }
        }
        return markerModels;
    }

    private static DotfileMarkerModel parseRawDocument(Path markerFilePath, Object rawDocument) {
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

        location = FormattingUtils.formatWithName(location, name); // resolve {NAME}
        location = FormattingUtils.formatWithHomeDirectory(location);

        PlatformOverrideModel linuxOverride = null;
        PlatformOverrideModel win32Override = null;
        PlatformOverrideModel darwinOverride = null;

        if (rawDocumentMap.containsKey("linux")) {
            linuxOverride = PlatformOverrideModel.parsePlatformRawSubdocument(name, rawDocumentMap.get("linux"));
        }

        if (rawDocumentMap.containsKey("win32")) {
            win32Override = PlatformOverrideModel.parsePlatformRawSubdocument(name, rawDocumentMap.get("win32"));
        }

        if (rawDocumentMap.containsKey("darwin")) {
            darwinOverride = PlatformOverrideModel.parsePlatformRawSubdocument(name, rawDocumentMap.get("darwin"));
        }

        return new DotfileMarkerModel(name, Path.of(location), markerFilePath, linuxOverride, win32Override, darwinOverride);
    }
}
