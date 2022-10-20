package com.falsepattern.jfunge.ip.impl;

import com.falsepattern.jfunge.ip.IStack;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true,
           chain = false)
public class Stack implements IStack {
    private final TIntList storage;
    @Setter
    @Getter
    private boolean invertMode;
    @Setter
    @Getter
    private boolean queueMode;

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

    public void clear() {
        storage.clear();
    }

    public int pick(int index) {
        return storage.get(storage.size() - 1 - index);
    }

    public int size() {
        return storage.size();
    }

    @Override
    public Stack deepCopy() {
        return new Stack(this);
    }
}
