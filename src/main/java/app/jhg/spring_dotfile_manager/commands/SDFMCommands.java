package app.jhg.spring_dotfile_manager.commands;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.command.annotation.Command;

@Configuration
public class SDFMCommands {
    
    @Command("init")
    public void init() {
        // TODO: initialize a config file with user input, default otherwise
    }

    
}
