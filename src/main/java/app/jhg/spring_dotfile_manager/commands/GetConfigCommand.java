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
