package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel.PlatformOverrideModel;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import app.jhg.spring_dotfile_manager.util.Os;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DotfileServiceImpl implements DotfileService {

    private final String dotfileGlobPattern;
    private final Os osName;

    private final ConfigService configService;
    private final FileService fileService;

    public DotfileServiceImpl(
        @Value("${spring-dotfile-manager.dotfile-glob-pattern}") String dotfileGlobPattern,
        @Value("${os.name}") String osName,
        ConfigService configService,
        FileService fileService
    ) {
        this.dotfileGlobPattern = dotfileGlobPattern;
        this.osName = FormattingUtils.getResolvedOsName(osName);
        this.configService = configService;
        this.fileService = fileService;
    }


    @Override
    public List<Path> getAllDotfileMarkerPaths() throws IOException {
        log.debug("Finding {} in {}", dotfileGlobPattern, configService.readDotfileRepoPath());
        // resolve to an absolute path: marker.sourceLocation is derived directly from these paths (see
        // DotfileMarkerModel), and a relative sourceLocation becomes the literal symlink target - which resolves
        // relative to the *link's* directory, not the process's cwd, producing a broken link
        Path dotfileRepoPath = Path.of(FormattingUtils.formatWithHomeDirectory(configService.readDotfileRepoPath()))
            .toAbsolutePath()
            .normalize();
        return fileService.glob(dotfileRepoPath, dotfileGlobPattern);
    }

    @Override
    public List<DotfileMarkerModel> getAllDotfileMarkerModels() throws IOException {
        List<Path> markerPaths = getAllDotfileMarkerPaths();
        List<DotfileMarkerModel> markerModels = new ArrayList<>();
        for (Path path : markerPaths) {
            log.debug("Processing marker file at {}", path);
            markerModels.addAll(getDotfileMarkerModelsByPath(path));
        }
        return markerModels;
    }

    @Override
    public List<DotfileMarkerModel> getDotfileMarkerModelsByPath(Path path) throws IOException {
        String content = fileService.readFile(path);
        log.debug("Creating markers from file at {}", path);
        return DotfileMarkerModel.fromMarkerFileContents(path, content);
    }

    @Override
    public void relinkDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathOrSkip(marker, "relink");
        if (locationForSystem == null) {
            return;
        }

        if (fileService.isSymbolicLink(locationForSystem)) {
            // happy path: unlink and re-create the link
            ensureSourceExists(marker, locationForSystem);
            log.debug("Deleting existing symlink at {} and creating new one for {}", locationForSystem, marker.sourceLocation);
            fileService.deleteFile(locationForSystem);
            fileService.createSymlink(locationForSystem, marker.sourceLocation);
        } else if (!fileService.exists(locationForSystem)) {
            // happy path 2: create the link if nothing exists
            ensureSourceExists(marker, locationForSystem);
            log.debug("No file at {}, creating symlink for {}", locationForSystem, marker.sourceLocation);
            Path parentDir = locationForSystem.getParent();
            if (parentDir != null) {
                fileService.createDirectories(parentDir);
            }
            fileService.createSymlink(locationForSystem, marker.sourceLocation);
        } else {
            // throw an exception, catch in the caller, and then prompt the user if they want to overwrite it
            log.debug("File exists at {}. Not overwriting in initial pass", locationForSystem);
            throw new FileAlreadyExistsException("Regular file/directory exists at " + locationForSystem + " and is not a symbolic link. Please move or delete it before relinking.");
        }
    }

    @Override
    public void overwriteExistingDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathOrSkip(marker, "relink");
        if (locationForSystem == null) {
            return;
        }

        ensureSourceExists(marker, locationForSystem);

        log.debug("Force deleting existing file at {} and relinking to {}", locationForSystem, marker.sourceLocation);
        fileService.forceDelete(locationForSystem);
        fileService.createSymlink(locationForSystem, marker.sourceLocation);
    }

    /**
     * refuses to proceed if the marker's source doesn't exist. A missing source becomes a dangling symlink on
     * POSIX systems, but on Windows the symlink type (file vs. directory) is inferred from whether the target
     * exists at creation time - a missing source is always linked as a file-type symlink, which permanently
     * cannot resolve into a directory even after the source is created. Checking up front avoids both.
     * @param marker the marker whose source should be checked
     * @param locationForSystem the link location, used only for the exception message
     * @throws NoSuchFileException if marker.sourceLocation does not exist
     */
    private void ensureSourceExists(DotfileMarkerModel marker, Path locationForSystem) throws NoSuchFileException {
        if (!fileService.exists(marker.sourceLocation)) {
            throw new NoSuchFileException(
                marker.sourceLocation.toString(),
                null,
                "Source does not exist; refusing to link " + locationForSystem
            );
        }
    }

    @Override
    public void unlinkDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathOrSkip(marker, "unlink");
        if (locationForSystem == null) {
            return;
        }
        
        if (fileService.isSymbolicLink(locationForSystem)) {
            log.debug("Unlinking symlink at {}", locationForSystem);
            fileService.deleteFile(locationForSystem);
        } else {
            log.debug("File exists at {} and is not a symbolic link. Cannot unlink.", locationForSystem);
            throw new FileAlreadyExistsException("Regular file/directory exists at " + locationForSystem + " and is not a symbolic link. Cannot unlink.");
        }
    }

    private Path getTargetPathOrSkip(DotfileMarkerModel marker, String action) {
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            log.debug("Dotfile {} is not applicable for this platform, skipping {}", marker.sourceLocation, action);
        }
        return locationForSystem;
    }

    @Override
    public Path getTargetPathForCurrentSystem(DotfileMarkerModel marker) {
        PlatformOverrideModel overrideModel = switch (osName) {
            case DARWIN -> {
                log.debug("Using Darwin for {}", marker.sourceLocation);
                yield marker.darwinOverride;
            }
            case WIN32 -> {
                log.debug("Using Windows for {}", marker.sourceLocation);
                yield marker.win32Override;
            }
            case LINUX -> {
                log.debug("Using Linux for {}", marker.sourceLocation);
                yield marker.linuxOverride;
            }
        };

        if (overrideModel == null) {
            log.debug("No override provided for {}, using default location", marker.sourceLocation);
            return marker.location; // no override -> use default location
        } else if (overrideModel.shouldLink) {
            // if there is an override and we should link on this platform, use the override location 
            log.debug("Using override location for {}: {}", marker.sourceLocation, overrideModel.location);
            return overrideModel.location;
        } else {
            // should not link -> null path
            log.debug("Not linking {} for this platform", marker.sourceLocation);
            return null;
        }
    }

    @Override
    public List<DotfileMarkerModel> getMarkersByKeyForCurrentSystem(String key) throws IOException {
        return getAllDotfileMarkerModels().stream()
            .filter(m -> key.equals(m.key))
            .filter(m -> getTargetPathForCurrentSystem(m) != null)
            .toList();
    }
}
