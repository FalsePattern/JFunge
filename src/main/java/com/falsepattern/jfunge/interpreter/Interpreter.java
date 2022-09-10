package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import com.falsepattern.jfunge.ip.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Accessors(fluent = true)
public class Interpreter implements ExecutionContext {
    public static final FileIOSupplier DEFAULT_FILE_IO_SUPPLIER = new FileIOSupplier() {
        @Override
        public byte[] readFile(String file) throws IOException {
            return Files.readAllBytes(Paths.get(file));
        }

        @Override
        public boolean writeFile(String file, byte[] data) throws IOException {
            Files.write(Paths.get(file), data);
            return true;
        }
    };
    @Getter
    private final FungeSpace fungeSpace = new FungeSpace(' ');

    private final List<InstructionPointer> IPs = new ArrayList<>();

    private final InstructionManager baseInstructionManager = new InstructionManager();

    private final InputStream input;


    @Getter
    private final OutputStream output;

    private final FileIOSupplier fileIOSupplier;

    @Getter
    private final List<String> args;

    @Getter
    private final int dimensions;

    private Integer exitCode = null;

    private InstructionPointer currentIP = null;

    private InstructionPointer clone = null;

    private int nextUUID = 0;

    private int inputStagger;


    public Interpreter(boolean trefunge, String[] args, InputStream input, OutputStream output, FileIOSupplier fileIOSupplier) {
        this.args = Arrays.asList(args);
        dimensions = trefunge ? 3 : 2;
        baseInstructionManager.loadInstructionSet(Funge98.INSTANCE);
        this.input = input;
        this.output = output;
        this.fileIOSupplier = fileIOSupplier;
        val ip = new InstructionPointer();
        ip.UUID = nextUUID++;
        IPs.add(ip);
    }

    public static int executeProgram(boolean trefunge, String[] args, byte[] program, long iterLimit, InputStream input, OutputStream output, FileIOSupplier fileIOSupplier) {
        val interpreter = new Interpreter(trefunge, args, input, output, fileIOSupplier);
        interpreter.fungeSpace().loadFileAt(0, 0, 0, program, trefunge);
        //Init step
        {
            val ip = interpreter.IPs.get(0);
            ip.position.sub(ip.delta);
            interpreter.step(ip);
        }
        if (iterLimit > 0) {
            long step = 0;
            while (!interpreter.stopped() && step < iterLimit) {
                interpreter.tick();
                step++;
            }
            if (!interpreter.stopped())
                throw new IllegalStateException("Program exceeded max iteration count!");
        } else {
            while (!interpreter.stopped()) {
                interpreter.tick();
            }
        }
        return interpreter.exitCode();
    }

    @Override
    public InstructionPointer[] allIPs() {
        return IPs.toArray(new InstructionPointer[0]);
    }

    @Override
    public InstructionPointer IP() {
        return currentIP;
    }

    @Override
    public InstructionPointer cloneIP() {
        clone = currentIP.deepCopy();
        clone.UUID = nextUUID++;
        return clone;
    }

    @Override
    public boolean stopped() {
        if (IPs.size() == 0) {
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
                if (opcode == 'r')
                    throw new IllegalArgumentException("Language does not implement 'r' reflect instruction.");
                interpret('r');
            }
        }
    }


    private boolean wrappingStep(InstructionPointer ip) {
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
            return true;
        }
        return false;
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
        int flipCount = 0;
        do {
            flipCount += wrappingStep(ip) ? 1 : 0;
            p = fungeSpace.get(ip.position);
            if (!ip.stringMode) {
                while (p == ';') {
                    do {
                        flipCount += wrappingStep(ip) ? 1 : 0;
                        p = fungeSpace.get(ip.position);
                    } while (p != ';');
                    flipCount += wrappingStep(ip) ? 1 : 0;
                    p = fungeSpace.get(ip.position);
                }
            }
            if (flipCount == 1000) {
                throw new IllegalStateException("IP " + ip.UUID + " is stuck on a blank strip!\nPos: " + ip.position + ", Delta: " + ip.delta + (ip.position.equals(0, 0, 0) && ip.UUID == 0 ? "\nIs the file empty?" : ""));
            }
        } while (p == ' ');
    }

    @Override
    public Map<String, String> env() {
        return Collections.unmodifiableMap(System.getenv());
    }

    @Override
    public int input(boolean stagger) {
        var value = -1;
        if (inputStagger > 0) {
            value = inputStagger;
            inputStagger = -1;
        } else {
            try {
                value = input.read();
            } catch (IOException ignored) {}
        }
        if (stagger) {
            inputStagger = value;
        }
        return value;
    }

    @Override
    public byte[] readFile(String file) {
        try {
            return fileIOSupplier.readFile(file);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean writeFile(String file, byte[] data) {
        try {
            return fileIOSupplier.writeFile(file, data);
        } catch (IOException e) {
            return false;
        }
    }

    public void tick() {
        currentIP = null;
        for (int i = 0; i < IPs.size(); i++) {
            currentIP = IPs.get(i);
            if (IP().isDead()) {
                IPs.remove(i);
                i--;
                continue;
            }
            interpret(fungeSpace().get(IP().position));
            if (clone != null) {
                IPs.add(i++, clone);
                clone = null;
            }
        }
        for (val ip : IPs) {
            step(ip);
        }
    }

    public interface FileIOSupplier {
        byte[] readFile(String file) throws IOException;

        boolean writeFile(String file, byte[] data) throws IOException;
    }
}
