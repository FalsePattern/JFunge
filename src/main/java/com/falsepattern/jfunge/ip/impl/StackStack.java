package com.falsepattern.jfunge.ip.impl;

import com.falsepattern.jfunge.ip.IStack;
import com.falsepattern.jfunge.ip.IStackStack;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

@NoArgsConstructor
@Accessors(fluent = true,
           chain = false)
public class StackStack implements IStackStack {
    private final Deque<IStack> stackStack = new ArrayDeque<>();
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private IStack TOSS = new Stack();

    @Getter
    private boolean invertMode;
    @Getter
    private boolean queueMode;

    private StackStack(StackStack original) {
        TOSS(original.TOSS().deepCopy());
        original.stackStack.forEach((stack) -> stackStack.add(stack.deepCopy()));
        invertMode = original.invertMode;
        queueMode = original.queueMode;
    }

    public Optional<IStack> SOSS() {
        return Optional.ofNullable(stackStack.peek());
    }

    public boolean push() {
        try {
            stackStack.push(TOSS);
            TOSS(new Stack());
            TOSS().invertMode(invertMode);
            TOSS().queueMode(queueMode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean pop() {
        if (stackStack.size() == 0) {
            return false;
        }
        TOSS(stackStack.pop());
        return true;
    }

    public void invertMode(boolean state) {
        invertMode = state;
        TOSS().invertMode(state);
        for (val stack : stackStack) {
            stack.invertMode(state);
        }
    }

    public void queueMode(boolean state) {
        queueMode = state;
        TOSS().queueMode(state);
        for (val stack : stackStack) {
            stack.queueMode(state);
        }
    }

    public int size() {
        return stackStack.size() + 1;
    }

    public int[] stackSizes() {
        val sizes = new int[stackStack.size() + 1];
        sizes[0] = TOSS().size();
        int i = 1;
        for (val s : stackStack) {
            sizes[i++] = s.size();
        }
        return sizes;
    }

    @Override
    public StackStack deepCopy() {
        return new StackStack(this);
    }
}
