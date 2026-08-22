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

package app.jhg.spring_dotfile_manager.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;
import app.jhg.spring_dotfile_manager.model.SubprocessResult;
import app.jhg.spring_dotfile_manager.util.FormattingUtils;
import app.jhg.spring_dotfile_manager.util.Os;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostInstallServiceImpl implements PostInstallService {

    private final String postInstallGlobPattern;
    private final Os osName;

    private final ConfigService configService;
    private final FileService fileService;
    private final SubprocessService subprocessService;

    public PostInstallServiceImpl(
        @Value("${spring-dotfile-manager.post-install-glob-pattern.linux}") String linuxGlobPattern,
        @Value("${spring-dotfile-manager.post-install-glob-pattern.darwin}") String darwinGlobPattern,
        @Value("${spring-dotfile-manager.post-install-glob-pattern.win32}") String win32GlobPattern,
        @Value("${os.name}") String osName,
        ConfigService configService,
        FileService fileService,
        SubprocessService subprocessService
    ) {
        this.osName = FormattingUtils.getResolvedOsName(osName);
        this.postInstallGlobPattern = switch (this.osName) {
            case LINUX  -> linuxGlobPattern;
            case DARWIN -> darwinGlobPattern;
            case WIN32  -> win32GlobPattern;
        };
        this.configService = configService;
        this.fileService = fileService;
        this.subprocessService = subprocessService;
    }

    @Override
    public List<Path> findPostInstallScripts() throws IOException {
        if (!configService.readAllowPostInstallScripts()) {
            // return an empty list immediately if post-install scripts are not allowed
            log.debug("Post-install scripts disabled");
            return List.of();
        }

        log.debug("Finding {} in {}", postInstallGlobPattern, configService.readDotfileRepoPath());
        Path dotfileRepoPath = Path.of(FormattingUtils.formatWithHomeDirectory(configService.readDotfileRepoPath()));
        List<Path> allPostInstallScripts = new ArrayList<>(fileService.glob(dotfileRepoPath, postInstallGlobPattern));
        allPostInstallScripts.sort(Comparator.naturalOrder());
        return allPostInstallScripts;
    }

    @Override
    public List<PostInstallScriptResult> runPostInstallScripts() throws IOException {
        List<Path> allPostInstallScripts = findPostInstallScripts();

        List<PostInstallScriptResult> results = new ArrayList<>();
        for (Path scriptPath : allPostInstallScripts) {
            log.debug("Starting execution for {}", scriptPath);
            List<String> cmd = switch (osName) {
                case LINUX, DARWIN -> List.of("bash", scriptPath.toString());
                case WIN32 -> List.of("pwsh", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", scriptPath.toString());
            };
            try {
                SubprocessResult res = subprocessService.executeCommand(scriptPath.getParent(), cmd);
                results.add(new PostInstallScriptResult(res.exitCode() == 0, res.output(), scriptPath));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                results.add(new PostInstallScriptResult(false, "Failed to run post-install script: " + e.getMessage(), scriptPath));
            } catch (IOException | ExecutionException | TimeoutException e) {
                results.add(new PostInstallScriptResult(false, "Failed to run post-install script: " + e.getMessage(), scriptPath));
            }
            log.debug("Finished execution for {}", scriptPath);
        }

        return results;
    }
}
