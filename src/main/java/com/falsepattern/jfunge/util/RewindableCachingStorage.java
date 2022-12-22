package com.falsepattern.jfunge.util;

import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RewindableCachingStorage<T> {
    private static final int EXTRA_CAPACITY = 16;

    private final Supplier<T> creator;
    private final Consumer<T> reseter;

    private final List<T> storage = new ArrayList<>();
    private final TIntStack marks = new TIntArrayStack();
    private int currentMark = 0;
    private int currentIndex = 0;


    public RewindableCachingStorage(Supplier<T> creator, Consumer<T> reseter) {
        this.creator = creator;
        this.reseter = reseter;
    }

    public T alloc() {
        T instance;
        if (storage.size() == currentIndex) {
            instance = creator.get();
            storage.add(instance);
            currentIndex++;
        } else {
            instance = storage.get(currentIndex++);
            reseter.accept(instance);
        }
        return instance;
    }

    public void mark() {
        marks.push(currentMark);
        currentMark = currentIndex;
    }

    public void unmark() {
        while (storage.size() - EXTRA_CAPACITY > currentIndex) {
            storage.remove(storage.size() - 1);
        }
        currentIndex = currentMark;
        currentMark = marks.pop();
    }
}
