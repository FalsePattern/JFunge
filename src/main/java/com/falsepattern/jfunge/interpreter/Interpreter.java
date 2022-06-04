package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;

import java.util.ArrayDeque;
import java.util.Deque;

@Accessors(fluent = true)
public class Interpreter implements ExecutionContext {

    @Getter
    private final FungeSpace fungeSpace = new FungeSpace(' ');

    private final InstructionPointer IP = new InstructionPointer();

    private final TIntObjectMap<Deque<Instruction>> instructionMap = new TIntObjectHashMap<>();

    @Getter
    private final int dimensions;

    public Interpreter(boolean trefunge) {
        dimensions = trefunge ? 3 : 2;
        new Funge98().load((instr, c) -> {
            var q = instructionMap.get(c);
            if (q == null) {
                instructionMap.put(c, q = new ArrayDeque<>());
            }
            q.push(instr);
        });
    }

    @Override
    public InstructionPointer[] allIPs() {
        return new InstructionPointer[]{IP};
    }

    @Override
    public InstructionPointer IP() {
        return IP;
    }

    @Override
    public boolean dead() {
        return IP.isDead();
    }

    @Override
    public void interpret(int opcode) {
        if (opcode == '"') {
            IP().stringMode = !IP().stringMode;
        } else if (IP().stringMode) {
            IP().stackStack.TOSS().push(opcode);
        } else {
            if (!instructionMap.containsKey(opcode)) {
                opcode = 'r';
            }
            instructionMap.get(opcode).peek().process(this);
        }
    }


    private void wrappingStep(InstructionPointer ip) {
        ip.position.add(ip.delta);
        if (!fungeSpace().bounds().inBounds(ip.position)) {
            ip.delta.mul(-1);
            do {
                ip.position.add(ip.delta);
            } while (!fungeSpace().bounds().inBounds(ip.position));
            do {
                ip.position.add(ip.delta);
            } while (fungeSpace().bounds().inBounds(ip.position));
            ip.delta.mul(-1);
            do {
                ip.position.add(ip.delta);
            } while (!fungeSpace().bounds().inBounds(ip.position));
        }
    }

    private void step(InstructionPointer ip) {
        int p;
        if (ip.stringMode) {
            p = fungeSpace.get(ip.position);
            if (p != ' ') {
                wrappingStep(ip);
                return;
            }
        }
        do {
            wrappingStep(ip);
            p = fungeSpace.get(ip.position);
            if (p == ';') {
                do {
                    wrappingStep(ip);
                    p = fungeSpace.get(ip.position);
                } while (p != ';');
            }
        } while (p == ' ');
    }

    public void tick() {
        for (InstructionPointer ip : allIPs()) {
            interpret(fungeSpace().get(ip.position));
            step(ip);
        }
    }
}
