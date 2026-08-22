# spring-dotfile-manager

A utility to manage mapping dotfiles from your repository to your system.

## Prerequisites:
- OpenJDK 21
- On Windows: Developer Mode enabled (**Settings > Privacy & security > For developers**), or an elevated (Administrator) terminal. Symlink creation requires `SeCreateSymbolicLinkPrivilege`, which Windows does not grant by default — see the note under [Creating Symlinks for Dotfiles](#creating-symlinks-for-dotfiles).

## Installing & Running

Start by cloning this repository. 

You can run commands from the repository itself, or build an executable JAR for your own use.

```sh
# builds an executable JAR and bash wrapper in ~/.local/bin
./bin/install.sh

# builds an executable JAR in `target/`
./mvnw clean package
java -jar ./target/spring-dotfile-manager-0.0.1-SNAPSHOT.jar ...

# use the Maven Spring Boot plugin
./mvnw spring-boot:run -Dspring-boot.run.arguments='...'
```

On Windows (PowerShell):

```powershell
# builds an executable JAR and sdfm.cmd wrapper in %LOCALAPPDATA%\spring-dotfile-manager\bin
.\bin\install.ps1

# builds an executable JAR in `target/`
.\mvnw.cmd clean package
java -jar .\target\spring-dotfile-manager-0.0.1-SNAPSHOT.jar ...

# use the Maven Spring Boot plugin
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments='...'
```

## Usage

### Initialize a Config File

```sh
sdfm init
```

Running the above command will generate an application config file at `~/.config/spring-dotfile-manager/config.yaml` on Unix-like systems, and `~/AppData/Local/spring-dotfile-manager/config.yaml` on Windows.

This config file will just be a two-line YAML document, which tells the application where your dotfile repository is located and whether post-install scripts should run. You can edit this file directly, or call `sdfm set-config <new_path_here>` to specify where your dotfile repository is located. Pass `--allow-post-install-scripts=true` (or `=false`) when initializing or updating your config to control whether post-install scripts run; an explicit `true`/`false` value is required.

A typical config file will look as follows. You can use `{HOME}` within this path, and `sdfm` will fill in your home directory at runtime.

```yaml
# {HOME} -> /home/user
dotfile-repo-path: "{HOME}/dotfiles"
allow-post-install-scripts: false
```

If this config file doesn't exist, you can specify the dotfile repository location as a CLI option. This will take precedence over the location specified in the config file, if it exists.

```sh
sdfm relink --dotfile-repo-path /home/user/dotfiles
```

This option is useful if you are setting up your dotfiles for the first time, and want to include the `sdfm` config in your dotfiles repository.

### Dotfile Marker Files

A dotfile marker file will sit in the same directory as a dotfile. It is formatted as a multi-document YAML file, with each document specifying one dotfile/folder in the current directory. For example, let's say our dotfile repo had the following structure:

```
.
├── git
│   └── .gitconfig
├── zsh
│   ├── .zshrc
│   └── .zprofile
└── nvim
    ├── init.lua
    └── ...
```

And we want these contents symlinked like this:

```
.
├── git
│   └── .gitconfig -> ~/.gitconfig
├── zsh
│   ├── .zshrc     -> ~/.zshrc
│   └── .zprofile  -> ~/.zprofile
└── nvim           -> ~/.config/nvim 
    └── ...        (^ linking a directory)
```

---

Let's start simple with the `git/` directory. Inside of `git/`, we will create a `.dotfiles` file:

```yaml
name: ".gitconfig"
location: "/home/user/.gitconfig"
```

This specifies that the `.gitconfig` file in the current directory should be symlinked to `/home/user/.gitconfig`. `name` should be the name of your file as it exists in your dotfiles repository, and `location` should be the full path of where you want to symlink that file.

---

Now, we can write a `.dotfiles` file for the `zsh/` directory. Even though there are multiple files in this directory, we only need to write **one** dotfile marker file.

This file will be a **multi-document** YAML file. We will use `---` to delimit between documents. **Each document will reference one dotfile**.

```yaml
name: ".zshrc"
location: "/home/user/.zshrc"
--- # starting a new document for the next dotfile!
name: ".zprofile"
location: "/home/user/.zprofile"
```

---

Finally, we will create a marker for the `nvim` ditectory. Neovim and some other programs might have their own **config directories**, with multiple files inside. We can **symlink the directory** as a whole, instead of specifying a symlink for each child.

An important note: since we are symlinking the *directory*, we will need to place our makrer file **at the same level**. That is, `.dotfiles` will sit alongside `nvim`, and both will share the same parent directory.

```yaml
name: "nvim"
location: "/home/user/.config/nvim"
```

---

With all of our markers created, we should now have the following structure:

```
.
├── git
│   ├── .dotfiles *
│   └── .gitconfig
├── zsh
│   ├── .dotfiles *
│   ├── .zshrc
│   └── .zprofile
├── .dotfiles *
└── nvim
    └── ...
```

### Creating Symlinks for Dotfiles

Once all of the dotfile marker files are created, we can start linking them from your repository to relevant locations on your file system! Running the following will scan your repository for markers, and then create symlinks based on your specifications:

```sh
sdfm relink
```

**Windows note:** creating symlinks requires the `SeCreateSymbolicLinkPrivilege` privilege, which Windows does not grant by default. Before running `sdfm relink` (or `unlink`) on Windows, either:
- enable Developer Mode (**Settings > Privacy & security > For developers**), or
- run `sdfm` from an elevated (Administrator) terminal.

Without one of the above, `relink`/`unlink` will fail with an error about a missing permission to create symbolic links.

### Unlinking Dotfiles

By default, any existing symlinks will be removed prior to creating a new symlink. Regular files and directories will **NOT** be removed and will generate an error (to be handled later). To remove all specified dotfiles without creating new links, run the following:

```sh
sdfm unlink
```

### Scoped Relink/Unlink with `--key`

By default, `relink` and `unlink` operate on every dotfile in your repository. To restrict either command to specific dotfiles, pass `--key`:

```sh
sdfm relink --key .zshrc
sdfm unlink --key .zshrc
```

Every dotfile has a `key`, which defaults to its `name` unless you set one explicitly in the marker file:

```yaml
name: ".zshrc"
location: "/home/user/.zshrc"
key: "shell-config"
```

`--key` can be passed multiple times to scope the command to several dotfiles at once:

```sh
sdfm relink --key shell-config --key nvim
```

If a given key doesn't match any dotfile applicable to your current platform, or matches more than one (ambiguous keys), `sdfm` will refuse to relink/unlink **any** of the requested keys and exit with an error — resolve the ambiguity (e.g. by giving one of the dotfiles a distinct `key`) before retrying. Run `sdfm list` to see a warning if any dotfiles share a key.

A scoped `relink` (i.e. one using `--key`) will not prompt to run post-install scripts, since those are meant to run after a full relink of your repository.

### Format Specifiers

To make writing these files simpler, we can use a couple of **format specifiers**. You can use `{HOME}` to inject your home folder on your current system. That way, you can use these marker files across multiple systems! This is useful when switching between Windows and other systems, or if you're on a computer where you have a different username. For example:

```yaml
name: ".gitconfig"
location: "{HOME}/.gitconfig"
```

This could translate to `C:/Users/user`, or `/Users/user`, or `/home/user`, depending on what system you're using.

We can also use `{NAME}` to avoid retyping the name of the file and possibly making a mistake. For example, the following config will still symlink the file to `/home/user/.gitconfig`:

```yaml
name: ".gitconfig"
location: "/home/user/{NAME}"
```

Multiple format specifiers can be used at once.

### Platform-Specific Overrides

In each marker document, you can specify additional keys for each of the supported platforms (`linux`, `darwin` (macOS), and `win32`) to customize the behavior of dotfiles on each platform.

Some files/directories that exist in `~/.config` on Unix-like systems are instead located at `~/AppData/Local` on Windows. By default, we might want a file to be linked into `~/.config`, but we want to override this behavior for Windows:

```yaml
name: "nvim"
location: "{HOME}/.config/nvim"
# override for windows
win32:
    shouldLink: true
    location: "{HOME}/AppData/Local/nvim"
```

When running on Windows, the `nvim` directory will be linked to `~/AppData/Local/nvim`, instead of its usual location.

`shouldLink` is a **required** boolean value, indicating whether a dotfile should be linked on the given platform.

`location` is an optional string value. If a dotfile is supposed to be linked on the current system, then this location will override the default. If this value is not present, then we will fall back to the default location.

Additionally, multiple platform overrides can be specified for a dotfile:

```yaml
name: ".zshrc"
location: "{HOME}/{NAME}"
win32:
    shouldLink: false
linux:
    shouldLink: false

---

name: ".bashrc"
location: "{HOME}/{NAME}"
win32:
    shouldLink: true
    location: "{HOME}/gitbash.bashrc"
darwin:
    shouldLink: false
```

### Post-Install Scripts

You can run a set of scripts after re-linking dotfiles to your system. By default, post-install scripts will not run. You can enable them by setting `allow-post-install-scripts` to `true` in your config file.

After dotfiles are re-linked, your dotfiles repository will be scanned for scripts inside of the `post-install` directory at the root of your repository. On Linux/macOS, post-install scripts are run via `bash <script>`, and only files ending in `.sh` are picked up. On Windows, they're run via `pwsh -File <script>`, and only files ending in `.ps1` are picked up — **PowerShell 7+ (`pwsh`) must be installed and on your `PATH`**; the older, preinstalled Windows PowerShell (`powershell`) is not used. You can have multiple subdirectories inside of the `post-install` directory. For example, in the below directory structure, only files marked with `*` will be executed:

```
.
├── ...
├── post-install
│   ├── script-01.sh *      (Linux/macOS)
│   ├── script-01.ps1 *     (Windows)
│   ├── subdir
│   │   └── script-02.sh *  (Linux/macOS)
│   └── some-program.exe
└── ...
```