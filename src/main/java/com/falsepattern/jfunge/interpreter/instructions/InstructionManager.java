package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.Copiable;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Deque;

@NoArgsConstructor
public class InstructionManager implements Copiable<InstructionManager> {
    private final TIntObjectMap<Deque<Instruction>> instructionMap = new TIntObjectHashMap<>();

    private InstructionManager(InstructionManager original) {
        original.instructionMap.forEachEntry((c, q) -> {
            val newQ = new ArrayDeque<Instruction>();
            q.forEach(newQ::push);
            instructionMap.put(c, newQ);
            return true;
        });
    }

    private void loadInstruction(Instruction instr, int c) {
        var q = instructionMap.get(c);
        if (q == null) {
            instructionMap.put(c, q = new ArrayDeque<>());
        }
        q.push(instr);
    }

    private Instruction unloadInstruction(int c) {
        Instruction instr = null;
        var q = instructionMap.get(c);
        if (q != null) {
            instr = q.pop();
            if (q.isEmpty()) {
                instructionMap.remove(c);
            }
        }
        return instr;
    }

    public void loadInstructionSet(InstructionSet set) {
        set.load(this::loadInstruction);
    }

    public void unloadInstructionSet(InstructionSet set) {
        set.unload(this::unloadInstruction);
    }

    public Instruction fetch(int c) {
        var q = instructionMap.get(c);
        if (q != null) {
            return q.peek();
        }
        return null;
    }

    @Override
    public InstructionManager deepCopy() {
        return new InstructionManager(this);
    }
}
