package com.falsepattern.jfunge.ip.impl;

import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import com.falsepattern.jfunge.ip.IP;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.joml.Vector3i;

import java.security.SecureRandom;

@Accessors(fluent = true,
           chain = false)
public class InstructionPointer implements IP {
    @Getter
    private final Vector3i position;
    @Getter
    private final Vector3i delta;
    @Getter
    private final Vector3i storageOffset;
    @Getter
    private final StackStack stackStack;
    @Getter
    private final InstructionManager instructionManager;
    @Getter
    private final TObjectIntMap<String> customStorage;
    private final SecureRandom rng;
    @Setter
    @Getter
    private boolean stringMode = false;
    @Setter
    @Getter
    private int UUID;
    @Getter
    private boolean dead = false;

    @SneakyThrows
    public InstructionPointer() {
        position = new Vector3i();
        delta = new Vector3i(1, 0, 0);
        storageOffset = new Vector3i();
        stackStack = new StackStack();
        instructionManager = new InstructionManager();
        customStorage = new TObjectIntHashMap<>();
        rng = SecureRandom.getInstanceStrong();
        UUID = 0;
    }

    @SneakyThrows
    private InstructionPointer(InstructionPointer original) {
        position = new Vector3i(original.position);
        delta = new Vector3i(original.delta);
        storageOffset = new Vector3i(original.storageOffset);
        stackStack = original.stackStack.deepCopy();
        dead = original.dead;
        stringMode = original.stringMode;
        instructionManager = original.instructionManager.deepCopy();
        customStorage = new TObjectIntHashMap<>(original.customStorage);
        rng = SecureRandom.getInstanceStrong();
        UUID = 0;
    }

    @Override
    public void die() {
        dead = true;
    }

    @Override
    public int nextRandom() {
        return rng.nextInt();
    }

    @Override
    public InstructionPointer deepCopy() {
        return new InstructionPointer(this);
    }
}
