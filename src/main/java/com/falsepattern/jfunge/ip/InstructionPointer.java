package com.falsepattern.jfunge.ip;

import lombok.NoArgsConstructor;
import org.joml.Vector3i;

@NoArgsConstructor
public class InstructionPointer {
    public final Vector3i position = new Vector3i();
    public final Vector3i delta = new Vector3i();
    public final StackStack stackStack = new StackStack();
}
