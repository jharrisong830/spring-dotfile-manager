package app.jhg.spring_dotfile_manager.config;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Component
public class DotfileRepoPathMixin {

    @Option(
        names = "--dotfile-repo-path",
        description = "Override the dotfile repository path (overrides config file).",
        scope = ScopeType.INHERIT
    )
    public String dotfileRepoPath;

    public String getDotfileRepoPath() {
        return dotfileRepoPath;
    }
}
