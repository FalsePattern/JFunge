package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Stream;

@NoArgsConstructor
@Accessors(fluent = true)
public class StackStack implements Copiable<StackStack> {
    private final Deque<Stack> stackStack = new ArrayDeque<>();
    @Getter
    @Setter(AccessLevel.PRIVATE)
    @NonNull
    private Stack TOSS = new Stack();

    private StackStack(StackStack original) {
        TOSS(original.TOSS().deepCopy());
        original.stackStack.forEach((stack) -> {
            stackStack.add(stack.deepCopy());
        });
    }

    public Optional<Stack> SOSS() {
        return Optional.ofNullable(stackStack.peek());
    }

    public boolean pushStackStack() {
        try {
            stackStack.push(TOSS);
            TOSS(new Stack());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int size() {
        return stackStack.size() + 1;
    }

    public int[] sizes() {
        return Stream.concat(Stream.of(TOSS), stackStack.stream()).mapToInt(Stack::size).toArray();
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
