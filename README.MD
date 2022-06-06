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

### Compatibility

The interpreter's handprint is `0xfa15e9a7` (`-99227225` in decimal), which is a hexadecimal approximation of "`falsepat`" from FalsePattern.

The version number given to befunge is `major * 256 * 256 + minor * 256 + patch`, where major, minor, patch are the 3
primary version numbers in the standard semver format.

The interpreter supports the following Funge-98 specification extensions:
- The entire Befunge base syntax (tested against Mycology and Mycorand)
- Buffered IO
- File IO with `i` and `o`
- System calls with `=` (paradigm 1, `system()`-like)
- Concurrency (pseudo-multithreading) with `t`
- Optional Trefunge mode (experimental)

Additionally, the following fingerprints are currently supported (more to come):
- [MODE](https://catseye.tc/view/funge-98/library/MODE.markdown)
- [MODU](https://catseye.tc/view/funge-98/library/MODU.markdown)
- [NULL](https://catseye.tc/view/funge-98/library/NULL.markdown)
- [ROMA](https://catseye.tc/view/funge-98/library/ROMA.markdown)

### References

- [Esolang wiki article of Funge-98](https://esolangs.org/wiki/Funge-98)
- [Official funge98 language specification](https://github.com/catseye/Funge-98/blob/master/doc/funge98.markdown)
- [Mycology Befunge-98 test suite](https://github.com/Deewiant/Mycology)
