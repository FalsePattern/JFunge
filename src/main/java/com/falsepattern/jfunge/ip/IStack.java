package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import lombok.SneakyThrows;
import lombok.val;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
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

    default void pushVecDimProof(int dimensions, Vector3i buf) {
        switch (dimensions) {
            default:
                throw new IllegalStateException("pushVecDimProof only works with parameters 1-3");
            case 1:
                push(buf.x);
                break;
            case 2:
                push(buf.x);
                push(buf.y);
                break;
            case 3:
                push(buf.x);
                push(buf.y);
                push(buf.z);
                break;
        }
    }

    default Vector3i popVecDimProof(int dimensions, Vector3i buf) {
        buf.set(0, 0, 0);
        switch (dimensions) {
            default:
                throw new IllegalStateException("popVecDimProof only works with parameters 1-3");
            case 3:
                buf.z = pop();
            case 2:
                buf.y = pop();
            case 1:
                buf.x = pop();
        }
        return buf;
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
        return data.toString(StandardCharsets.UTF_8);
    }

    default void pushL(long value) {
        push((int) (value & 0xFFFFFFFFL));
        push((int) ((value >> 32) & 0xFFFFFFFFL));
    }

    default long popL() {
        return ((long) pop() << 32) | (pop() & 0xFFFFFFFFL);
    }

    default void pushF(float val) {
        push(Float.floatToRawIntBits(val));
    }

    default float popF() {
        return Float.intBitsToFloat(pop());
    }

    default void pushD(double val) {
        pushL(Double.doubleToRawLongBits(val));
    }

    default double popD() {
        return Double.longBitsToDouble(popL());
    }

    default void pushF2(Vector2fc v) {
        pushF(v.x());
        pushF(v.y());
    }

    default Vector2f popF2(Vector2f v) {
        v.y = popF();
        v.x = popF();
        return v;
    }

    default void pushF3(Vector3fc v) {
        pushF(v.x());
        pushF(v.y());
        pushF(v.z());
    }

    default Vector3f popF3(Vector3f v) {
        v.z = popF();
        v.y = popF();
        v.x = popF();
        return v;
    }

    default void pushF4(Vector4fc v) {
        pushF(v.x());
        pushF(v.y());
        pushF(v.z());
        pushF(v.w());
    }

    default Vector4f popF4(Vector4f v) {
        v.w = popF();
        v.z = popF();
        v.y = popF();
        v.x = popF();
        return v;
    }
}
