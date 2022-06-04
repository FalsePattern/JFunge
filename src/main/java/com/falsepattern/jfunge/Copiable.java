package com.falsepattern.jfunge;

public interface Copiable<T extends Copiable<T>> {
    T deepCopy();
}
