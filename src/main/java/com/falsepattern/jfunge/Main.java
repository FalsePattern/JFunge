package com.falsepattern.jfunge;

import com.falsepattern.jfunge.interpreter.FeatureSet;
import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        val options = new Options();
        val masterGroup = new OptionGroup();
        masterGroup.setRequired(false);
        masterGroup.addOption(Option.builder("f")
                                    .longOpt("file")
                                    .hasArg(true)
                                    .argName("file")
                                    .numberOfArgs(1)
                                    .desc("The file to load into the interpreter at the origin on startup.")
                                    .build());
        masterGroup.addOption(Option.builder()
                                    .longOpt("version")
                                    .desc("Prints the current program version, along with the handprint and version given by befunge's y instruction.")
                                    .build());
        masterGroup.addOption(Option.builder().longOpt("license").desc("Prints the license of the program.").build());
        masterGroup.addOption(Option.builder().longOpt("help").desc("Displays this help page").build());
        options.addOptionGroup(masterGroup);
        options.addOption(null, "trefunge", false,
                          "Enable 3D (Trefunge) mode. By default, the interpreter emulates 2D Befunge for compatibility.");
        options.addOption("t", "concurrent", false,
                          "Enables the Concurrent Funge extension (t instruction). Buggy programs can potentially forkbomb the interpreter.");
        options.addOption(null, "env", false,
                          "Allows the interpreter to access the environment variables of the host system.");
        options.addOption(null, "syscall", false,
                          "Enables the syscall feature (= instruction). This is a very dangerous permission to grant, it can call any arbitrary program on your system.");
        options.addOption(Option.builder("i")
                                .longOpt("readperm")
                                .hasArg(true)
                                .argName("file")
                                .numberOfArgs(-2)
                                .desc("Enables read access to the specified file or directory (i instruction). Specify / to allow read access to every file on the system (dangerous). Can specify multiple files/folders.")
                                .build());
        options.addOption(Option.builder("o")
                                .longOpt("writeperm")
                                .hasArg(true)
                                .argName("file")
                                .numberOfArgs(-2)
                                .desc("Enables write access to the specified file or directory (o instruction). Specify / to allow write access to every file on the system (dangerous). Can specify multiple files/folders.")
                                .build());
        options.addOption(Option.builder()
                                .longOpt("maxiter")
                                .hasArg(true)
                                .argName("iterations")
                                .numberOfArgs(1)
                                .desc("The maximum number of iterations the program can run for. Anything less than 1 will run until the program terminates by itself. Default is unlimited.")
                                .build());
        options.addOption(Option.builder()
                                .longOpt("perl")
                                .desc("Enable the PERL fingerprint. This requires the working directory of the interpreter to be writable, and is also an arbitrary code execution risk.")
                                .build());
        options.addOption(Option.builder()
                                .longOpt("sock")
                                .desc("Enable the SOCK and SCKE fingerprints. This allows the program to open a socket and listen for connections, as well as connect to external hosts. This is a very dangerous permission to grant, it can potentially allow remote code execution.")
                                .build());
        val parser = new DefaultParser();
        val cmd = parser.parse(options, args);
        if (cmd.hasOption("help")) {
            val formatter = new HelpFormatter();
            formatter.printHelp(80, "jfunge", "JFunge, a Funge98 interpeter for java.", options, null, true);
            return;
        }
        if (cmd.hasOption("license")) {
            try (val res = Main.class.getResourceAsStream("/LICENSE")) {
                if (res == null) {
                    System.out.println(
                            "Could not read embedded license file, however, this program (JFunge) is licensed under LGPLv3.");
                    return;
                }
                val buf = new byte[256];
                int read;
                while ((read = res.read(buf)) > 0) {
                    System.out.write(buf, 0, read);
                }
                System.out.println();
                return;
            }
        }
        if (cmd.hasOption("version")) {
            System.out.println("Version: " + Globals.VERSION);
            System.out.println("Handprint: 0x" + Integer.toHexString(Globals.HANDPRINT));
            System.out.println("FungeVersion: 0x" + Integer.toHexString(Globals.FUNGE_VERSION));
            return;
        }
        if (!cmd.hasOption("f")) {
            System.out.println("No file specified. See --help");
            return;
        }
        val file = cmd.getOptionValue("f");
        val featureSet = FeatureSet.builder();
        featureSet.trefunge(cmd.hasOption("trefunge"));
        featureSet.concurrent(cmd.hasOption("t"));
        featureSet.environment(cmd.hasOption("env"));
        featureSet.allowedInputFiles(cmd.getOptionValues("i"));
        featureSet.allowedOutputFiles(cmd.getOptionValues("o"));
        featureSet.perl(cmd.hasOption("perl"));
        featureSet.maxIter(cmd.hasOption("maxiter") ? Integer.parseInt(cmd.getOptionValue("maxiter")) : 0);
        byte[] program;
        if (file.equals("-")) {
            val in = System.in;
            val programBytes = new ByteArrayOutputStream();
            var read = 0;
            val buf = new byte[4096];
            while ((read = in.read(buf)) > 0) {
                programBytes.write(buf, 0, read);
            }
            program = programBytes.toByteArray();
        } else {
            program = Files.readAllBytes(Paths.get(file));
        }
        int returnCode =
                Interpreter.executeProgram(args, program, System.in, System.out, Interpreter.DEFAULT_FILE_IO_SUPPLIER,
                                           featureSet.build());
        System.out.flush();
        System.exit(returnCode);
    }
}
