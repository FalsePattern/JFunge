package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.Getter;
import org.joml.Vector3i;

public class InstructionPointer implements Copiable<InstructionPointer> {
    public final Vector3i position;
    public final Vector3i delta;
    public final Vector3i storageOffset;
    public final StackStack stackStack;
    public final InstructionManager instructionManager;
    public final TObjectIntMap<String> customStorage;
    @Getter
    private boolean dead = false;
    public boolean stringMode = false;
    public int UUID;

    public InstructionPointer() {
        position = new Vector3i();
        delta = new Vector3i(1, 0, 0);
        storageOffset = new Vector3i();
        stackStack = new StackStack();
        instructionManager = new InstructionManager();
        customStorage = new TObjectIntHashMap<>();
    }

    private InstructionPointer(InstructionPointer original) {
        position = new Vector3i(original.position);
        delta = new Vector3i(original.delta);
        storageOffset = new Vector3i(original.storageOffset);
        stackStack = original.stackStack.deepCopy();
        dead = original.dead;
        stringMode = original.stringMode;
        instructionManager = original.instructionManager.deepCopy();
        customStorage = new TObjectIntHashMap<>(original.customStorage);
        UUID = 0;
    }

    public void die() {
        dead = true;
    }

    @Override
    public InstructionPointer deepCopy() {
        return new InstructionPointer(this);
    }
}
