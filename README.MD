# JFunge
A standard-conforming Befunge-98 and Trefunge-98 interpreter.

### Usage
`jfunge [file] [--3d] [--version] [--license]`

| argument    | usage                                                                                                     |
|-------------|-----------------------------------------------------------------------------------------------------------|
| `file`      | The file to load into the interpreter                                                                     |
| `--3d`      | Enable 3D (Trefunge) mode. By default, the interpreter acts as if it was 2D for compatibility reasons.    |
| `--version` | Prints the current program version, along with the handprint and version given by befunge's y instruction |
| `--license` | Prints the license of the program.                                                                        |

### Currently implemented fingerprints
- MODU
- NULL
- ROMA

### Additional goals
- [ ] File IO
- [ ] Concurrency
