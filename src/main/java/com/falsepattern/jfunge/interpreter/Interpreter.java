package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.Fingerprint;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.MODU;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.NULL;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.ROMA;
import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;

import java.util.*;

@Accessors(fluent = true)
public class Interpreter implements ExecutionContext {
    private static final TIntObjectMap<Fingerprint> fingerprints = new TIntObjectHashMap<>();

    @Getter
    private final FungeSpace fungeSpace = new FungeSpace(' ');

    private final InstructionPointer IP = new InstructionPointer();

    private final InstructionManager baseInstructionManager = new InstructionManager();

    @Getter
    private final List<String> args;

    @Getter
    private final int dimensions;

    static {
        addFingerprint(MODU.INSTANCE);
        addFingerprint(NULL.INSTANCE);
        addFingerprint(ROMA.INSTANCE);
    }

    private static void addFingerprint(Fingerprint print) {
        fingerprints.put(print.code(), print);
    }

    public Interpreter(boolean trefunge, String[] args) {
        this.args = Arrays.asList(args);
        dimensions = trefunge ? 3 : 2;
        baseInstructionManager.loadInstructionSet(Funge98.INSTANCE);
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
            if (opcode == '(' || opcode == ')') {
                int n = IP().stackStack.TOSS().pop();
                int sum = 0;
                for (int i = 0; i < n; i++) {
                    sum *= 256;
                    sum += IP().stackStack.TOSS().pop();
                }
                if (fingerprints.containsKey(sum)) {
                    val p = fingerprints.get(sum);
                    if (opcode == '(') {
                        IP().stackStack.TOSS().push(sum);
                        IP().stackStack.TOSS().push(1);
                        IP().instructionManager.loadInstructionSet(p);
                    } else {
                        IP().instructionManager.unloadInstructionSet(p);
                    }
                } else {
                    interpret('r');
                }
            } else {
                Instruction instr;
                if ((instr = IP().instructionManager.fetch(opcode)) != null) {
                    instr.process(this);
                } else if ((instr = baseInstructionManager.fetch(opcode)) != null) {
                    instr.process(this);
                } else {
                   baseInstructionManager.fetch('r').process(this);
                }
            }
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

    public void step(InstructionPointer ip) {
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
            if (!ip.stringMode) {
                while (p == ';') {
                    do {
                        wrappingStep(ip);
                        p = fungeSpace.get(ip.position);
                    } while (p != ';');
                    wrappingStep(ip);
                    p = fungeSpace.get(ip.position);
                }
            }
        } while (p == ' ');
    }

    @Override
    public Map<String, String> env() {
        return Collections.unmodifiableMap(System.getenv());
    }

    @Override
    public int paradigm() {
        return 0;
    }

    public void tick() {
        for (InstructionPointer ip : allIPs()) {
            interpret(fungeSpace().get(ip.position));
            step(ip);
        }
    }
}
