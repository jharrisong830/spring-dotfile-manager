package app.jhg.spring_dotfile_manager.commands;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "get-config",
    description = "Get the current dotfile repository configuration",
    mixinStandardHelpOptions = true
)
@Slf4j
public class GetConfigCommand implements Callable<Integer> {
    
    private final ConfigService configService;

    public GetConfigCommand(ConfigService configService) {
        this.configService = configService;
    }
    
    @Override
    public Integer call() throws Exception {
        configService.printConfig();
        return 0;
    }
}
