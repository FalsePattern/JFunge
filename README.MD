# JFunge
A standard-conforming Funge-98 interpreter.

### Usage
`jfunge [file] [--3d] [--version] [--license]`

| argument             | usage                                                                                                                           |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `file`               | The file to load into the interpreter                                                                                           |
| `--3d`               | Enable 3D (Trefunge) mode. By default, the interpreter emulates 2D Befunge for compatibility.                                   |
| `--version`          | Prints the current program version, along with the handprint and version given by befunge's y instruction                       |
| `--license`          | Prints the license of the program.                                                                                              |
| `--maxiter <number>` | The maximum number of iterations the program can run for. Anything less than 1 will run until the program terminates naturally. |

### References
- [Esolang wiki article of Funge-98](https://esolangs.org/wiki/Funge-98)
- [Official funge98 language specification](https://github.com/catseye/Funge-98/blob/master/doc/funge98.markdown)
- [Mycology Befunge-98 test suite](https://github.com/Deewiant/Mycology)

### Currently implemented fingerprints
- [MODE](https://catseye.tc/view/funge-98/library/MODE.markdown)
- [MODU](https://catseye.tc/view/funge-98/library/MODU.markdown)
- [NULL](https://catseye.tc/view/funge-98/library/NULL.markdown)
- [ROMA](https://catseye.tc/view/funge-98/library/ROMA.markdown)

### Additional goals
- [x] File IO
- [x] Concurrency
