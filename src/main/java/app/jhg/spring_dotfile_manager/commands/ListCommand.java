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

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.model.DotfileMarkerModel;
import app.jhg.spring_dotfile_manager.service.DotfileService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "list", description = "List all dotfiles in the configured repository that would be linked on your current system", mixinStandardHelpOptions = true)
@Slf4j
public class ListCommand implements Callable<Integer> {

    @Option(names = { "-a",
            "--all" }, description = "Include all dotfiles and their detailed configuration, regardless of platform", defaultValue = "false")
    private boolean includeAll;

    private final DotfileService dotfileService;

    public ListCommand(DotfileService dotfileService) {
        this.dotfileService = dotfileService;
    }

    @Override
    public Integer call() throws Exception {
        List<DotfileMarkerModel> markers = dotfileService.getAllDotfileMarkerModels();

        if (markers.isEmpty()) {
            log.info("No dotfiles found in the configured repository.");
            log.info("Please add some dotfiles to your repository and try again.");
            return 1;
        }

        if (!includeAll) {
            log.debug("Filtering to dotfiles relevant for the current platform...");
            markers = markers.stream()
                    .filter(m -> dotfileService.getTargetPathForCurrentSystem(m) != null)
                    .toList();

            findDuplicateKeys(markers).forEach((key, sourceLocations) ->
                log.warn("Multiple dotfiles share the key '{}': {}. A scoped relink/unlink with --key '{}' would refuse to run until this is resolved.",
                    key, sourceLocations, key));
        }

        if (markers.isEmpty()) {
            log.info("No dotfiles found in the configured repository for the current system.");
            log.info("Use --all option to see all dotfiles regardless of platform.");
            return 1;
        } else {
            log.info("Dotfiles in configured repository:");
            if (includeAll) {
                log.info("(Showing all dotfiles regardless of platform)");
            }
            for (DotfileMarkerModel marker : markers) {
                if (includeAll) {
                    log.info("- {}", marker);
                } else {
                    Path currentPlatformPath = dotfileService.getTargetPathForCurrentSystem(marker);
                    log.info("{}", marker.prettyPrint(currentPlatformPath));
                }
            }
        }

        return 0;
    }

    /**
     * groups the given markers by key, keeping only keys shared by more than one marker.
     * @param markers the markers to check, expected to already be filtered to those applicable to the current platform
     * @return a map of key to the source locations of the markers sharing that key, for keys with more than one match
     */
    private Map<String, List<Path>> findDuplicateKeys(List<DotfileMarkerModel> markers) {
        return markers.stream()
                .collect(Collectors.groupingBy(
                    m -> m.key,
                    LinkedHashMap::new,
                    Collectors.mapping(m -> m.sourceLocation, Collectors.toList())
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }
}
