// spring-dotfile-manager
// Copyright (C) 2026  John Graham

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package app.jhg.spring_dotfile_manager.commands;

import app.jhg.spring_dotfile_manager.service.ConfigService;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.util.concurrent.Callable;

@Component
@Command(
    name = "init",
    description = "Initialize the configuration for Spring Dotfile Manager",
    mixinStandardHelpOptions = true
)
@Slf4j
public class InitCommand implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "Path to your dotfile repository",
        defaultValue = ""
    )
    private String dotfileRepoPath;

    @Option(
        names = "--allow-post-install-scripts",
        arity = "1",
        description = "Whether to allow the execution of post-install scripts (true or false)"
    )
    private Boolean allowPostInstallScripts;

    private final String defaultDotfileRepoPath;
    private final boolean defaultAllowPostInstall;

    private final ConfigService configService;
    private final BufferedReader stdinReader;

    public InitCommand(
        @Value("${spring-dotfile-manager.config.default-repo-path}") String defaultDotfileRepoPath,
        @Value("${spring-dotfile-manager.config.default-allow-post-install}") boolean defaultAllowPostInstall,
        ConfigService configService,
        BufferedReader stdinReader
    ) {
        this.defaultDotfileRepoPath = defaultDotfileRepoPath;
        this.defaultAllowPostInstall = defaultAllowPostInstall;
        this.configService = configService;
        this.stdinReader = stdinReader;
    }

    @Override
    public Integer call() throws Exception {
        dotfileRepoPath = dotfileRepoPath.trim();

        if (dotfileRepoPath.isEmpty()) {
            log.info("No dotfile repository path provided.");
            log.info("Enter desired path, or <Enter> to accept default ({})", FormattingUtils.formatWithHomeDirectory(defaultDotfileRepoPath));

            String line = stdinReader.readLine();
            String customPath = line != null ? line.trim() : "";

            if (!customPath.isEmpty()) {
                log.debug("Using custom dotfile repository path");
                dotfileRepoPath = customPath;
            } else {
                log.debug("Using DEFAULT path");
                dotfileRepoPath = defaultDotfileRepoPath;
            }
        }

        if (allowPostInstallScripts == null) {
            log.debug("Using DEFAULT allow post install");
            allowPostInstallScripts = defaultAllowPostInstall;
        }

        log.debug("Setting dotfile repository path to: {}", dotfileRepoPath);
        configService.initializeConfig(dotfileRepoPath, allowPostInstallScripts);
        configService.printConfig();
        return 0;
    }
}
