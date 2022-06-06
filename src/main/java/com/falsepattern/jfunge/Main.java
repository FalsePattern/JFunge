package com.falsepattern.jfunge;

import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;
import lombok.var;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        if (args.length < 1) {
            System.out.println("Usage: jfunge [file] [--3d] [--version] [--license] [--maxiter <number>]\nfile  The file to load into the interpreter\n--3d   Enable 3D (Trefunge) mode. By default, the interpreter acts as if it was 2D for compatibility reasons.\n--version  Prints the current program version, along with the handprint and version given by befunge's y instruction\n--license  Prints the license text of the program.\n--maxiter <number>  The maximum number of iterations the program can run for. Anything less than 1 will run until the program terminates naturally.");
        }
        if (argsList.remove("--license")) {
            System.out.println(Globals.LICENSE);
        }
        if (argsList.remove("--version")) {
            System.out.println("Version: " + Globals.VERSION);
            System.out.println("Handprint: 0x" + Integer.toHexString(Globals.HANDPRINT));
            System.out.println("FungeVersion: 0x" + Integer.toHexString(Globals.FUNGE_VERSION));
        }
        val trefunge = argsList.remove("--3d");
        var maxIter = 0L;
        if (argsList.contains("--maxiter")) {
            val iterIndex = argsList.indexOf("--maxiter") + 1;
            if (argsList.size() <= iterIndex) {
                System.err.println("Please specify a number after --maxiter!");
                System.exit(-1);
            }
            val maxIterText = argsList.remove(iterIndex);
            try {
                maxIter = Long.parseLong(maxIterText);
            } catch (NumberFormatException e) {
                System.err.println(maxIterText + " is an invalid number!");
            }
            argsList.remove("--maxiter");
        }
        if (argsList.size() == 0) {
            System.out.println("No file specified.");
            return;
        }
        val file = argsList.remove(0);
        if (argsList.size() != 0) {
            System.out.println("Extraneous arguments ignored:");
            argsList.forEach(System.out::println);
            System.out.println();
        }
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
        System.exit(Interpreter.executeProgram(trefunge, args, program, maxIter, System.in, System.out, Interpreter.DEFAULT_FILE_IO_SUPPLIER));
    }
}
