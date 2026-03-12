package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;

@Service
public class DotfileServiceImpl implements DotfileService {
    
    private final String dotfileGlobPattern;

    private final ConfigService configService;
    private final FileService fileService;
    private final FormatterService formatterService;

    public DotfileServiceImpl(
        @Value("${spring-dotfile-manager.dotfile-glob-pattern}") String dotfileGlobPattern,
        ConfigService configService, 
        FileService fileService,
        FormatterService formatterService
    ) {
        this.dotfileGlobPattern = dotfileGlobPattern;
        this.configService = configService;
        this.fileService = fileService;
        this.formatterService = formatterService;
    }


    @Override
    public List<Path> getAllDotfileMarkerPaths() throws IOException {
        Path dotfileRepoPath = Path.of(formatterService.formatWithHomeDirectory(configService.readConfig()));
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
        String content = formatterService.formatWithHomeDirectory(fileService.readFile(path));
        return DotfileMarkerModel.fromMarkerFileContents(path, content);
    }
}
