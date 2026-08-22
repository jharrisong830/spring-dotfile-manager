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

import org.springframework.stereotype.Component;

import app.jhg.spring_dotfile_manager.config.DebugMixin;
import app.jhg.spring_dotfile_manager.config.DotfileRepoPathMixin;
import app.jhg.spring_dotfile_manager.config.VersionProviderConfiguration;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Mixin;

@Component
@Command(
    name = "sdfm",
    versionProvider = VersionProviderConfiguration.class,
    subcommands = {
        InitCommand.class,
        GetConfigCommand.class,
        SetConfigCommand.class,
        ListCommand.class,
        RelinkCommand.class,
        UnlinkCommand.class,
        HelpCommand.class
    },
    mixinStandardHelpOptions = true
)
public class RootCommand {

    @Mixin
    private DebugMixin debugMixin;

    @Mixin
    private DotfileRepoPathMixin dotfileRepoPathMixin;
}
