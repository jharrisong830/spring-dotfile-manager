package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

public class DotfileServiceImpl implements DotfileService {
    
    private final String dotfileGlobPattern;

    private final ConfigService configService;
    private final FileService fileService;
    private final FormatterService formatterService;

    public DotfileServiceImpl(
        @Value("${spring-dotfile-manager.config.dotfile-glob-pattern}") String dotfileGlobPattern,
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
}
