//package app.jhg.spring_dotfile_manager.commands;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.List;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
//import app.jhg.spring_dotfile_manager.service.ConfigService;
//import app.jhg.spring_dotfile_manager.service.DotfileService;
//import app.jhg.spring_dotfile_manager.util.FormattingUtils;
//
//@Component
//@Slf4j
//public class SDFMCommands {
//
//    private final String defaultRepoPath;
//
//    private final ConfigService configService;
//    private final DotfileService dotfileService;
//    private final BufferedReader stdinReader;
//
//    public SDFMCommands(
//        @Value("${spring-dotfile-manager.config.default-repo-path}") String defaultRepoPath,
//        ConfigService configService,
//        DotfileService dotfileService,
//        BufferedReader stdinReader
//    ) {
//        this.defaultRepoPath = defaultRepoPath;
//        this.configService = configService;
//        this.dotfileService = dotfileService;
//        this.stdinReader = stdinReader;
//    }
//
//
////    @Command(name = "init", description = "Initialize the configuration for Spring Dotfile Manager", exitStatusExceptionMapper = "exitStatusExceptionMapper")
//    public void init(
////        @Argument(
////            index = 0,
////            description = "Path to your dotfile repository",
////            defaultValue = ""
////        ) String dotfileRepoPath
//        String dotfileRepoPath
//    ) throws Exception {
//        dotfileRepoPath = dotfileRepoPath.trim();
//        if (dotfileRepoPath.isEmpty()) {
//            log.info("No dotfile repository path provided.");
//            log.info("Enter desired path, or <Enter> to accept default ({})", FormattingUtils.formatWithHomeDirectory(defaultRepoPath));
//
//            String line = stdinReader.readLine();
//            String customPath = line != null ? line.trim() : "";
//
//            if (!customPath.isEmpty()) {
//                dotfileRepoPath = customPath;
//            } else {
//                dotfileRepoPath = defaultRepoPath;
//            }
//        }
//
//        configService.initializeConfig(dotfileRepoPath);
//        printConfig(dotfileRepoPath);
//    }
//
////    @Command(name = "get-config", description = "Get the current dotfile repository configuration", exitStatusExceptionMapper = "exitStatusExceptionMapper")
//    public void getConfig() throws IOException {
//        String dotfileRepoPath = configService.readConfig();
//        printConfig(dotfileRepoPath);
//    }
//
////    @Command(name = "set-config", description = "Set the dotfile repository path in the configuration", exitStatusExceptionMapper = "exitStatusExceptionMapper")
//    public void setConfig(
////        @Argument(
////            index = 0,
////            description = "New dotfile repository path"
////        ) String dotfileRepoPath
//        String dotfileRepoPath
//    ) throws Exception {
//        dotfileRepoPath = dotfileRepoPath.trim();
//        if (dotfileRepoPath.isEmpty()) {
//            throw new IllegalArgumentException("Dotfile repository path cannot be empty. Please provide a valid path.");
//        }
//
//        configService.updateConfig(dotfileRepoPath);
//        printConfig(dotfileRepoPath);
//    }
//
////    @Command(name = "list", description = "List all dotfiles in the configured repository", exitStatusExceptionMapper = "exitStatusExceptionMapper")
//    public void list() throws Exception {
//        List<DotfileMarkerModel> markerModels = dotfileService.getAllDotfileMarkerModels();
//        if (markerModels.isEmpty()) {
//            log.info("No dotfiles found in the configured repository.");
//        } else {
//            log.info("Dotfiles in configured repository:");
//            for (DotfileMarkerModel model : markerModels) {
//                log.info("- {}", model);
//            }
//        }
//    }
//
////    @Command(name = "relink", description = "Relink all dotfiles in the configured repository", exitStatusExceptionMapper = "exitStatusExceptionMapper")
//    public void relink() throws Exception {
//        List<DotfileMarkerModel> markerModels = dotfileService.getAllDotfileMarkerModels();
//        if (markerModels.isEmpty()) {
//            log.info("No dotfiles found to relink in the configured repository.");
//        } else {
//            log.info("Relinking dotfiles in configured repository...");
//            dotfileService.relinkDotfiles();
//        }
//    }
//
//
//    private void printConfig(String dotfileRepoPath) {
//        log.info("Configuration at: {}", configService.getConfigFilePath());
//        log.info("Using dotfile repository path: '{}'", FormattingUtils.formatWithHomeDirectory(dotfileRepoPath));
//    }
//}
