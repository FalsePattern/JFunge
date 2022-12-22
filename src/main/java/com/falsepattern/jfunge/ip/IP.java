package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import gnu.trove.map.TObjectIntMap;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;

public interface IP extends Copiable<IP> {
    Vector3i position();
    Vector3i delta();
    Vector3i storageOffset();
    IStackStack stackStack();
    InstructionManager instructionManager();
    TObjectIntMap<String> customStorage();

    void stringMode(boolean state);
    boolean stringMode();

    int UUID();
    void UUID(int newUUID);

    int nextRandom();

    boolean dead();
    void die();

    default void step() {
        position().add(delta());
    }

    default void reflect() {
        delta().mul(-1);
    }

    //QoL code
    default Vector2ic position2(Vector2i buf) {
        buf.x = position().x;
        buf.y = position().y;
        return buf;
    }

    default Vector2ic position2() {
        return position2(new Vector2i());
    }

    default Vector2ic delta2(Vector2i buf) {
        buf.x = delta().x;
        buf.y = delta().y;
        return buf;
    }

    default Vector2ic delta2() {
        return delta2(new Vector2i());
    }

    default Vector2ic storageOffset2(Vector2i buf) {
        buf.x = storageOffset().x;
        buf.y = storageOffset().y;
        return buf;
    }

    default Vector2ic storageOffset2() {
        return storageOffset2(new Vector2i());
    }
}
