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

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class PicocliRunner implements ApplicationRunner, ExitCodeGenerator {

    private int exitCode;

    private final CommandLine commandLine;

    public PicocliRunner(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public void run(ApplicationArguments args) {
        exitCode = commandLine.execute(args.getSourceArgs());
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
