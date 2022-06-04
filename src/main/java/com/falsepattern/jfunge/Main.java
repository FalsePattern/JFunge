package com.falsepattern.jfunge;

import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: jfunge <file> [--3d]\n--3d   Enable 3D (Trefunge) mode. By default, the interpreter acts as if it was 2D for compatibility reasons.");
        }
        val file = args[0];
        val trefunge = args.length >= 2 && args[1].equals("--3d");
        val interpreter = new Interpreter(trefunge, args);
        interpreter.fungeSpace().loadFileAt(0, 0, 0, Files.readAllBytes(Paths.get(file)), trefunge);
        while (!interpreter.dead()) {
            interpreter.tick();
        }
    }
}
