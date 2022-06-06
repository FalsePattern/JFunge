package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.instructions.InstructionSet;

public interface Fingerprint extends InstructionSet {
    int code();
}
