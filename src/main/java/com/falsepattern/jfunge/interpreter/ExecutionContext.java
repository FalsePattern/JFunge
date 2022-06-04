package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;

public interface ExecutionContext {
    InstructionPointer[] allIPs();
    InstructionPointer IP();
    FungeSpace fungeSpace();
    int dimensions();
    boolean dead();
    void interpret(int code);
}
