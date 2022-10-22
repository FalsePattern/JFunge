# JFunge

A standard-conforming Funge-98 interpreter.

### Usage
```
usage: jfunge [-f <file> | --help | --license | --version]  [-i <file>]
       [--maxiter <iterations>] [-o <file>] [--syscall] [-t] [--trefunge]
JFunge, a Funge98 interpeter for java.
 -f,--file <file>            The file to load into the interpreter at the origin
                             on startup.
    --help                   Displays this help page
 -i,--readperm <file>        Enables read access to the specified file or
                             directory (i instruction). Specify / to allow read
                             access to every file on the system (dangerous). Can
                             specify multiple files/folders.
    --license                Prints the license of the program.
    --maxiter <iterations>   The maximum number of iterations the program can
                             run for. Anything less than 1 will run until the
                             program terminates by itself. Default is unlimited.
 -o,--writeperm <file>       Enables write access to the specified file or
                             directory (o instruction). Specify / to allow write
                             access to every file on the system (dangerous). Can
                             specify multiple files/folders.
    --syscall                Enables the syscall feature (= instruction). This
                             is a very dangerous permission to grant, it can
                             call any arbitrary program on your system.
 -t,--concurrent             Enables the Concurrent Funge extension (t
                             instruction). Buggy programs can potentially
                             forkbomb the interpreter.
    --trefunge               Enable 3D (Trefunge) mode. By default, the
                             interpreter emulates 2D Befunge for compatibility.
    --version                Prints the current program version, along with the
                             handprint and version given by befunge's y
                             instruction.
```

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
- [3DSP](http://rcfunge98.com/rcsfingers.html#3DSP)
- [FPSP](http://rcfunge98.com/rcsfingers.html#FPSP)
- [HRTI](./docs/catseye/library/HRTI.markdown)
- [MODE](./docs/catseye/library/MODE.markdown)
- [MODU](./docs/catseye/library/MODU.markdown)
- [NULL](./docs/catseye/library/NULL.markdown)
- [ORTH](./docs/catseye/library/ORTH.markdown)
- [PERL](./docs/catseye/library/PERL.markdown)
- [REFC](./docs/catseye/library/REFC.markdown)
- [ROMA](./docs/catseye/library/ROMA.markdown)
- [TOYS](./docs/catseye/library/TOYS.markdown)

### Version Release Checklist
- Update the version number inside the [pom](./pom.xml)
- Update the version string and FUNGE_VERSION inside [Globals](./src/main/java/com/falsepattern/jfunge/Globals.java)
- Create a new git tag with the same version number, and push it to master
- Never push tags outside master!

### References

- [Esolang wiki article of Funge-98](https://esolangs.org/wiki/Funge-98)
- [Official funge98 language specification](./docs/catseye/doc/funge98.markdown)
- [Mycology Befunge-98 test suite](https://github.com/Deewiant/Mycology)
