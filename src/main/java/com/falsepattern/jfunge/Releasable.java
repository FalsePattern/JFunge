package com.falsepattern.jfunge;

public interface Releasable extends AutoCloseable{
    /**
     * When called, it signals that this specific object is no longer referenced anywhere in the code, and can be freely deleted or even reused later by the provider.
     */
    void release();

    @Override
    default void close() {
        release();
    }
}
