package com.falsepattern.jfunge.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class MemoryStack implements AutoCloseable{
    private static final ThreadLocal<MemoryStack> THREAD_LOCAL = ThreadLocal.withInitial(MemoryStack::new);

    private int depth = 0;

    private final RewindableCachingStorage<Matrix4f> mat4f = new RewindableCachingStorage<>(Matrix4f::new,
                                                                                            Matrix4f::identity);
    private final RewindableCachingStorage<Vector3i> vec3i = new RewindableCachingStorage<>(Vector3i::new,
                                                                                            Vector3i::zero);
    private final RewindableCachingStorage<Vector3f> vec3f = new RewindableCachingStorage<>(Vector3f::new,
                                                                                            Vector3f::zero);

    public static MemoryStack stackPush() {
        return THREAD_LOCAL.get().push();
    }

    public Matrix4f mat4f() {
        return mat4f.alloc();
    }

    public Vector3i vec3i() {
        return vec3i.alloc();
    }

    public Vector3f vec3f() {
        return vec3f.alloc();
    }

    public MemoryStack push() {
        mat4f.mark();
        vec3i.mark();
        vec3f.mark();
        depth++;
        return this;
    }

    public void pop() {
        if (depth == 0) {
            throw new IllegalStateException("Cannot pop stack of depth 0!");
        }
        depth--;
        mat4f.unmark();
        vec3i.unmark();
        vec3f.unmark();
    }

    @Override
    public void close() {
        pop();
    }
}