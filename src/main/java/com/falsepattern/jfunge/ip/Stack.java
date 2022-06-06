package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.val;
import org.joml.*;

public class Stack implements Copiable<Stack> {
    private final TIntList storage;
    public boolean invertMode;
    public boolean queueMode;
    public Stack() {
        storage = new TIntArrayList();
    }

    private Stack(Stack original) {
        storage = new TIntArrayList(original.storage);
        invertMode = original.invertMode;
        queueMode = original.queueMode;
    }

    public void push(int x) {
        if (invertMode) {
            storage.insert(0, x);
        } else {
            storage.add(x);
        }
    }

    public int peek() {
        int s = storage.size();
        return s == 0 ? 0 : storage.get(queueMode ? 0 : s - 1);
    }

    public int pop() {
        int s = storage.size();
        return s == 0 ? 0 : storage.removeAt(queueMode ? 0 : s - 1);
    }

    public void push2(Vector2ic v) {
        storage.add(v.x());
        storage.add(v.y());
    }

    public Vector2i pop2(Vector2i v) {
        v.y = pop();
        v.x = pop();
        return v;
    }

    public Vector2i pop2() {
        return pop2(new Vector2i());
    }

    public void push3(Vector3ic v) {
        storage.add(v.x());
        storage.add(v.y());
        storage.add(v.z());
    }

    public Vector3i pop3(Vector3i v) {
        v.z = pop();
        v.y = pop();
        v.x = pop();
        return v;
    }

    public Vector3i pop3() {
        return pop3(new Vector3i());
    }

    public void push4(Vector4ic v) {
        storage.add(v.x());
        storage.add(v.y());
        storage.add(v.z());
        storage.add(v.w());
    }

    public Vector4i pop4(Vector4i v) {
        v.w = pop();
        v.z = pop();
        v.y = pop();
        v.x = pop();
        return v;
    }

    public Vector4i pop4() {
        return pop4(new Vector4i());
    }

    public void clear() {
        storage.clear();
    }

    public int pick(int index) {
        return storage.get(storage.size() - 1 - index);
    }

    public void pushString(String text) {
        val chars = text.toCharArray();
        push(0);
        for (int i = chars.length - 1; i >= 0; i--) {
            push(chars[i]);
        }
    }

    public String popString() {
        val sb = new StringBuilder();
        char c;
        while ((c = (char)pop()) != 0) {
            sb.append(c);
        }
        return sb.toString();
    }

    public int size() {
        return storage.size();
    }

    @Override
    public Stack deepCopy() {
        return new Stack(this);
    }
}
