package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;

@Service
public class DotfileServiceImpl implements DotfileService {
    
    private final String dotfileGlobPattern;

    private final ConfigService configService;
    private final FileService fileService;

    public DotfileServiceImpl(
        @Value("${spring-dotfile-manager.dotfile-glob-pattern}") String dotfileGlobPattern,
        ConfigService configService,
        FileService fileService
    ) {
        this.dotfileGlobPattern = dotfileGlobPattern;
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
        if (fileService.isSymbolicLink(marker.location)) {
            // happy path: unlink and re-create the link
            fileService.deleteFile(marker.location);
            fileService.createSymlink(marker.location, marker.sourceLocation);
        } else if (!fileService.exists(marker.location)) {
            // happy path 2: create the link if nothing exists
            fileService.createSymlink(marker.location, marker.sourceLocation);
        } else {
            // throw an exception, catch in the caller, and then prompt the user if they want to overwrite it
            throw new FileAlreadyExistsException("Regular file/directory exists at " + marker.location + " and is not a symbolic link. Please move or delete it before relinking.");
        }
    }

    @Override
    public void overwriteExistingDotfile(DotfileMarkerModel marker) throws IOException {
        fileService.forceDelete(marker.location);
        fileService.createSymlink(marker.location, marker.sourceLocation);
    }

    @Override
    public void unlinkDotfile(DotfileMarkerModel marker) throws IOException {
        if (fileService.isSymbolicLink(marker.location)) {
            fileService.deleteFile(marker.location);
        } else {
            throw new FileAlreadyExistsException("Regular file/directory exists at " + marker.location + " and is not a symbolic link. Cannot unlink.");
        }
    }
}
