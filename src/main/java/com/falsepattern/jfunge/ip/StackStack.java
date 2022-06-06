package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

@NoArgsConstructor
@Accessors(fluent = true)
public class StackStack implements Copiable<StackStack> {
    private final Deque<Stack> stackStack = new ArrayDeque<>();
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private Stack TOSS = new Stack();

    @Getter
    private boolean invertMode;
    @Getter
    private boolean queueMode;

    private StackStack(StackStack original) {
        TOSS(original.TOSS().deepCopy());
        original.stackStack.forEach((stack) -> {
            stackStack.add(stack.deepCopy());
        });
        invertMode = original.invertMode;
        queueMode = original.queueMode;
    }

    public Optional<Stack> SOSS() {
        return Optional.ofNullable(stackStack.peek());
    }

    public boolean pushStackStack() {
        try {
            stackStack.push(TOSS);
            TOSS(new Stack());
            TOSS().invertMode = invertMode;
            TOSS().queueMode = queueMode;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void invertMode(boolean state) {
        invertMode = state;
        TOSS().invertMode = state;
        for (Stack stack : stackStack) {
            stack.invertMode = state;
        }
    }

    public void queueMode(boolean state) {
        queueMode = state;
        TOSS().queueMode = state;
        for (Stack stack : stackStack) {
            stack.queueMode = state;
        }
    }

    public int size() {
        return stackStack.size() + 1;
    }

    public int[] sizes() {
        val sizes = new int[stackStack.size() + 1];
        sizes[0] = TOSS().size();
        int i = 1;
        for (val s: stackStack) {
            sizes[i++] = s.size();
        }
        return sizes;
    }

    public boolean popStackStack() {
        if (stackStack.size() == 0) return false;
        TOSS(stackStack.pop());
        return true;
    }

    @Override
    public StackStack deepCopy() {
        return new StackStack(this);
    }
}
