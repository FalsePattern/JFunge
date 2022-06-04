package com.falsepattern.jfunge.ip;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joml.Vector3i;

@NoArgsConstructor
public class InstructionPointer {
    public final Vector3i position = new Vector3i();
    public final Vector3i delta = new Vector3i(1, 0, 0);
    public final StackStack stackStack = new StackStack();
    @Getter
    private boolean dead = false;
    public boolean stringMode = true;

    public void die() {
        dead = true;
    }
}
