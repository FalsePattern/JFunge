package com.falsepattern.jfunge.ip;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

@Accessors(fluent = true)
public class StackStack {
    private final Deque<Stack> stackStack = new ArrayDeque<>();
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private Stack TOSS = new Stack();

    public Optional<Stack> SOSS() {
        return Optional.ofNullable(stackStack.peek());
    }

    public boolean pushStackStack() {
        stackStack.push(TOSS);
        TOSS(new Stack());
        return true;
    }

    public boolean popStackStack() {
        if (stackStack.size() == 0) return false;
        TOSS(stackStack.pop());
        return true;
    }
}
