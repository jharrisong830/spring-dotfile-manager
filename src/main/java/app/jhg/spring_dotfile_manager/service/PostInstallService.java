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

import app.jhg.spring_dotfile_manager.model.PostInstallScriptResult;

public interface PostInstallService {

    /**
     * locates and runs all bash scripts using the pattern `post-install/\*\*\/\*\.sh`
     * @return list of post install results for each script run, indicating whether it was successful, and any command output details
     * @throws IOException if there are issues reading from the user's config file
     */
    List<PostInstallScriptResult> runPostInstallScripts() throws IOException;

    /**
     * locates all bash scripts using the pattern `post-install/\*\*\/\*\.sh`, without running them, sorted for deterministic execution order
     * @return list of discovered post-install script paths, or an empty list if post-install scripts are disabled or the current platform is Windows (not yet supported, since scripts require bash)
     * @throws IOException if there are issues reading from the user's config file or scanning the dotfile repository
     */
    List<Path> findPostInstallScripts() throws IOException;
}
