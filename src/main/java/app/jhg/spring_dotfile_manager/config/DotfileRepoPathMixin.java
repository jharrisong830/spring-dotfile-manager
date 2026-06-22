package app.jhg.spring_dotfile_manager.config;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Component
@Slf4j
public class DotfileRepoPathMixin {

    @Getter
    @Option(
        names = "--dotfile-repo-path",
        description = "Override the dotfile repository path (overrides config file).",
        scope = ScopeType.INHERIT
    )
    public String dotfileRepoPath;
}
