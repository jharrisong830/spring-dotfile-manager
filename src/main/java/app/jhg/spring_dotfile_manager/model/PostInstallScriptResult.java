package app.jhg.spring_dotfile_manager.model;

import java.nio.file.Path;

public record PostInstallScriptResult(boolean success, String message, Path script) {}
