package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface ExecutionContext {
    InstructionPointer[] allIPs();
    InstructionPointer IP();
    InstructionPointer cloneIP();
    FungeSpace fungeSpace();
    int dimensions();
    boolean stopped();
    void stop(int exitCode);
    int exitCode();
    void interpret(int code);
    void step(InstructionPointer ip);
    List<String> args();
    Map<String, String> env();
    int paradigm();
    InputStream input();
    OutputStream output();
}
