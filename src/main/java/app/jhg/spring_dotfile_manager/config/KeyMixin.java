package app.jhg.spring_dotfile_manager.config;

import org.springframework.stereotype.Component;

import lombok.Getter;
import picocli.CommandLine.Option;

@Component
public class KeyMixin {

    @Getter
    @Option(
        names = "--key",
        description = "Restrict the operation to the dotfile with the given key, instead of all dotfiles in the repository."
    )
    public String key;
}
