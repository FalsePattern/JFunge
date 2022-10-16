package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface ExecutionContext {
    InstructionPointer[] allIPs();

    InstructionPointer IP();

    InstructionPointer cloneIP();

    FungeSpace fungeSpace();

    <T> T getGlobal(int finger, String key);

    <T> void putGlobal(int finger, String key, T value);

    boolean hasGlobal(int finger, String key);

    int dimensions();

    boolean stopped();

    void stop(int exitCode);

    int exitCode();

    void interpret(int code);

    void step(InstructionPointer ip);

    List<String> args();

    Map<String, String> env();

    int input(boolean stagger);

    OutputStream output();

    byte[] readFile(String file);

    boolean writeFile(String file, byte[] data);

    int envFlags();

    default boolean concurrentAllowed() {
        return (envFlags() & 0x01) != 0;
    }

    default boolean fileInputAllowed(String path) throws IOException {
        return (envFlags() & 0x02) != 0;
    }

    default boolean fileOutputAllowed(String path) throws IOException {
        return (envFlags() & 0x04) != 0;
    }

    default boolean syscallAllowed() {
        return (envFlags() & 0x08) != 0;
    }
}
