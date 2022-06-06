package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Accessors(fluent = true)
public class Interpreter implements ExecutionContext {
    @Getter
    private final FungeSpace fungeSpace = new FungeSpace(' ');

    private final InstructionPointer IP = new InstructionPointer();

    private final InstructionManager baseInstructionManager = new InstructionManager();

    @Getter
    private final InputStream input;

    @Getter
    private final OutputStream output;

    @Getter
    private final List<String> args;

    @Getter
    private final int dimensions;

    private Integer exitCode = null;

    public static int executeProgram(boolean trefunge, String[] args, byte[] program, long iterLimit, InputStream input, OutputStream output) {
        val interpreter = new Interpreter(trefunge, args, input, output);
        interpreter.fungeSpace().loadFileAt(0, 0, 0, program, trefunge);
        if (iterLimit > 0) {
            long step = 0;
            while (!interpreter.stopped() && step < iterLimit) {
                interpreter.tick();
                step++;
            }
            if (!interpreter.stopped()) throw new IllegalStateException("Program exceeded max iteration count!");
        } else {
            while (!interpreter.stopped()) {
                interpreter.tick();
            }
        }
        return interpreter.exitCode();
    }

    public Interpreter(boolean trefunge, String[] args, InputStream input, OutputStream output) {
        this.args = Arrays.asList(args);
        dimensions = trefunge ? 3 : 2;
        baseInstructionManager.loadInstructionSet(Funge98.INSTANCE);
        this.input = input;
        this.output = output;
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
    public boolean stopped() {
        if (dead()) {
            exitCode = 0;
        }
        return exitCode != null;
    }

    @Override
    public void stop(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public int exitCode() {
        return exitCode == null ? 0 : exitCode;
    }

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
            Instruction instr;
            if ((instr = IP().instructionManager.fetch(opcode)) != null || (instr = baseInstructionManager.fetch(opcode)) != null) {
                instr.process(this);
            } else {
                if (opcode == 'r') throw new IllegalArgumentException("Language does not implement 'r' reflect instruction.");
                interpret('r');
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
