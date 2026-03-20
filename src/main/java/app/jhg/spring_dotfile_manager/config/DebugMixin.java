package app.jhg.spring_dotfile_manager.config;

import picocli.CommandLine;

public class DebugMixin {

    @CommandLine.Option(
        names = "--debug",
        defaultValue="false"
    )
    public void configureDebugLogging() {/* TODO */}
}
