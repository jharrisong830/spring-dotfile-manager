package app.jhg.spring_dotfile_manager.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Getter;
import picocli.CommandLine.Option;

@Component
public class KeyMixin {

    @Getter
    @Option(
        names = "--key",
        description = "Restrict the operation to the dotfile(s) with the given key, instead of all dotfiles in the repository. May be specified multiple times"
    )
    public List<String> keys = new ArrayList<>();
}
