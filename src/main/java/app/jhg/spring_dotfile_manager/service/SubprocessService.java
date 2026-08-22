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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import app.jhg.spring_dotfile_manager.model.SubprocessResult;

public interface SubprocessService {
    
    /**
     * executes a command in the given working directory
     * @param cwd current working directory to operate in
     * @param args command as a list of strings
     * @return the result of the subprocess execution, including exit code and captured output
     * @throws IllegalArgumentException if args is empty
     */
    SubprocessResult executeCommand(Path cwd, List<String> args) throws IOException, InterruptedException, ExecutionException, TimeoutException;
}
