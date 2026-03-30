package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel.PlatformOverrideModel;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DotfileServiceImpl implements DotfileService {
    
    private final String dotfileGlobPattern;
    private final String osName;

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
        log.debug("Finding {} in {}", dotfileGlobPattern, configService.readConfig());
        Path dotfileRepoPath = Path.of(FormattingUtils.formatWithHomeDirectory(configService.readConfig()));
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
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            log.debug("Dotfile {} is not applicable for this platform, skipping relink", marker.sourceLocation);
            return;
        }

        if (fileService.isSymbolicLink(locationForSystem)) {
            // happy path: unlink and re-create the link
            log.debug("Deleting existing symlink at {} and creating new one for {}", locationForSystem, marker.sourceLocation);
            fileService.deleteFile(locationForSystem);
            fileService.createSymlink(locationForSystem, marker.sourceLocation);
        } else if (!fileService.exists(locationForSystem)) {
            // happy path 2: create the link if nothing exists
            log.debug("No file at {}, creating symlink for {}", locationForSystem, marker.sourceLocation);
            fileService.createSymlink(locationForSystem, marker.sourceLocation);
        } else {
            // throw an exception, catch in the caller, and then prompt the user if they want to overwrite it
            log.debug("File exists at {}. Not overwriting in initial pass", locationForSystem);
            throw new FileAlreadyExistsException("Regular file/directory exists at " + locationForSystem + " and is not a symbolic link. Please move or delete it before relinking.");
        }
    }

    @Override
    public void overwriteExistingDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            log.debug("Dotfile {} is not applicable for this platform, skipping relink", marker.sourceLocation);
            return;
        }
        
        log.debug("Force deleting existing file at {} and relinking to {}", locationForSystem, marker.sourceLocation);
        fileService.forceDelete(locationForSystem);
        fileService.createSymlink(locationForSystem, marker.sourceLocation);
    }

    @Override
    public void unlinkDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            log.debug("Dotfile {} is not applicable for this platform, skipping unlink", marker.sourceLocation);
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

    @Override
    public Path getTargetPathForCurrentSystem(DotfileMarkerModel marker) {
        PlatformOverrideModel overrideModel;

        if (osName.equals("darwin")) {
            log.debug("Using Darwin for {}", marker.sourceLocation);
            overrideModel = marker.darwinOverride;
        } else if (osName.equals("win32")) {
            log.debug("Using Windows for {}", marker.sourceLocation);
            overrideModel = marker.win32Override;
        } else { // osName.equals("linux")
            log.debug("Using Linux for {}", marker.sourceLocation);
            overrideModel = marker.linuxOverride;
        }

        if (overrideModel == null) {
            log.debug("No override provided for {}, using default location", marker.sourceLocation);
            return marker.location; // no override -> use default location
        } else if (overrideModel.shouldLink) {
            // if there is an override and we should link on this platform,
            // use the override location if it exists, otherwise, link with the default location
            log.debug("Using override location for {}: {}", marker.sourceLocation, overrideModel.location);
            return (overrideModel.location == null) ? marker.location : overrideModel.location;
        } else {
            // should not link -> null path
            log.debug("Not linking {} for this platform", marker.sourceLocation);
            return null;
        }
    }
}
