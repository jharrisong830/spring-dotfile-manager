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

@Service
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

        if (osName.contains("Linux")) {
            this.osName = "linux";
        } else if (osName.contains("Mac")) {
            this.osName = "darwin";
        } else if (osName.contains("Win")) {
            this.osName = "win32";
        } else {
            throw new UnsupportedOperationException("Unsupported OS for dotfile linking: " + osName);
        }
        
        this.configService = configService;
        this.fileService = fileService;
    }


    @Override
    public List<Path> getAllDotfileMarkerPaths() throws IOException {
        Path dotfileRepoPath = Path.of(FormattingUtils.formatWithHomeDirectory(configService.readConfig()));
        return fileService.glob(dotfileRepoPath, dotfileGlobPattern);
    }

    @Override
    public List<DotfileMarkerModel> getAllDotfileMarkerModels() throws IOException {
        List<Path> markerPaths = getAllDotfileMarkerPaths();
        List<DotfileMarkerModel> markerModels = new ArrayList<>();
        for (Path path : markerPaths) {
            markerModels.addAll(getDotfileMarkerModelsByPath(path));
        }
        return markerModels;
    }

    @Override
    public List<DotfileMarkerModel> getDotfileMarkerModelsByPath(Path path) throws IOException {
        String content = fileService.readFile(path);
        return DotfileMarkerModel.fromMarkerFileContents(path, content);
    }

    @Override
    public void relinkDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            return;
        }

        if (fileService.isSymbolicLink(locationForSystem)) {
            // happy path: unlink and re-create the link
            fileService.deleteFile(locationForSystem);
            fileService.createSymlink(locationForSystem, marker.sourceLocation);
        } else if (!fileService.exists(locationForSystem)) {
            // happy path 2: create the link if nothing exists
            fileService.createSymlink(locationForSystem, marker.sourceLocation);
        } else {
            // throw an exception, catch in the caller, and then prompt the user if they want to overwrite it
            throw new FileAlreadyExistsException("Regular file/directory exists at " + locationForSystem + " and is not a symbolic link. Please move or delete it before relinking.");
        }
    }

    @Override
    public void overwriteExistingDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            return;
        }
        
        fileService.forceDelete(locationForSystem);
        fileService.createSymlink(locationForSystem, marker.sourceLocation);
    }

    @Override
    public void unlinkDotfile(DotfileMarkerModel marker) throws IOException {
        Path locationForSystem = getTargetPathForCurrentSystem(marker);
        if (locationForSystem == null) {
            // if the marker shouldn't be linked on this platform, do nothing
            return;
        }
        
        if (fileService.isSymbolicLink(locationForSystem)) {
            fileService.deleteFile(locationForSystem);
        } else {
            throw new FileAlreadyExistsException("Regular file/directory exists at " + locationForSystem + " and is not a symbolic link. Cannot unlink.");
        }
    }

    @Override
    public Path getTargetPathForCurrentSystem(DotfileMarkerModel marker) {
        PlatformOverrideModel overrideModel;

        if (osName.equals("darwin")) {
            overrideModel = marker.darwinOverride;
        } else if (osName.equals("win32")) {
            overrideModel = marker.win32Override;
        } else { // osName.equals("linux")
            overrideModel = marker.linuxOverride;
        }

        if (overrideModel == null) {
            return marker.location; // no override -> use default location
        } else if (overrideModel.shouldLink) {
            // if there is an override and we should link on this platform,
            // use the override location if it exists, otherwise, link with the default location
            return (overrideModel.location == null) ? marker.location : overrideModel.location;
        } else {
            // should not link -> null path
            return null;
        }
    }
}
