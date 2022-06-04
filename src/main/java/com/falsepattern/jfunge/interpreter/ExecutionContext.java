package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;

import java.util.List;
import java.util.Map;

public interface ExecutionContext {
    InstructionPointer[] allIPs();
    InstructionPointer IP();
    FungeSpace fungeSpace();
    int dimensions();
    boolean dead();
    void interpret(int code);
    void step(InstructionPointer ip);
    List<String> args();
    Map<String, String> env();
    int paradigm();
}
