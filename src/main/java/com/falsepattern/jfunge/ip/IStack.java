package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4i;
import org.joml.Vector4ic;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public interface IStack extends Copiable<IStack> {
    void push(int x);
    int peek();
    int pop();
    void clear();
    int pick(int index);
    int size();
    void invertMode(boolean state);
    boolean invertMode();
    void queueMode(boolean state);
    boolean queueMode();

    default void push2(Vector2ic v) {
        push(v.x());
        push(v.y());
    }

    default Vector2i pop2(Vector2i v) {
        v.y = pop();
        v.x = pop();
        return v;
    }

    default Vector2i pop2() {
        return pop2(new Vector2i());
    }

    default void push3(Vector3ic v) {
        push(v.x());
        push(v.y());
        push(v.z());
    }

    default Vector3i pop3(Vector3i v) {
        v.z = pop();
        v.y = pop();
        v.x = pop();
        return v;
    }

    default Vector3i pop3() {
        return pop3(new Vector3i());
    }

    default void push4(Vector4ic v) {
        push(v.x());
        push(v.y());
        push(v.z());
        push(v.w());
    }

    default Vector4i pop4(Vector4i v) {
        v.w = pop();
        v.z = pop();
        v.y = pop();
        v.x = pop();
        return v;
    }

    default Vector4i pop4() {
        return pop4(new Vector4i());
    }

    default void pushString(String text) {
        val chars = text.getBytes(StandardCharsets.UTF_8);
        push(0);
        for (int i = chars.length - 1; i >= 0; i--) {
            push(chars[i]);
        }
    }

    @SneakyThrows
    default String popString() {
        val data = new ByteArrayOutputStream();
        byte b;
        while ((b = (byte) pop()) != 0) {
            data.write(b);
        }
        return data.toString("UTF-8");
    }
}
