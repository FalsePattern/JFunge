package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.PERL;
import com.falsepattern.jfunge.ip.IP;
import com.falsepattern.jfunge.ip.impl.InstructionPointer;
import com.falsepattern.jfunge.storage.FungeSpace;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final List<IP> IPs = new ArrayList<>();

    private final InstructionManager baseInstructionManager = new InstructionManager();

    private final InputStream input;

    private final TIntList fingerprintBlackList = new TIntArrayList();

    @Getter
    private final OutputStream output;

    private final FileIOSupplier fileIOSupplier;

    @Getter
    private final List<String> args;

    @Getter
    private final int dimensions;

    @Getter
    private final int envFlags;

    private final boolean unrestrictedInput;
    private final Path[] allowedInputPaths;

    private final boolean unrestrictedOutput;
    private final Path[] allowedOutputPaths;

    private final TIntObjectMap<Map<String, Object>> globals = new TIntObjectHashMap<>();

    private Integer exitCode = null;

    private IP currentIP = null;

    private IP clone = null;

    private int nextUUID = 0;

    private int inputStagger;

    public Interpreter(String[] args, InputStream input, OutputStream output, FileIOSupplier fileIOSupplier, FeatureSet featureSet) {
        this.args = Arrays.asList(args);
        dimensions = featureSet.trefunge ? 3 : 2;
        baseInstructionManager.loadInstructionSet(Funge98.INSTANCE);
        this.input = input;
        this.output = output;
        this.fileIOSupplier = fileIOSupplier;
        val ip = (IP) new InstructionPointer();
        ip.UUID(nextUUID++);
        IPs.add(ip);
        int env = 0;
        if (featureSet.concurrent) {
            env |= 1;
        }
        if (featureSet.allowedInputFiles != null && featureSet.allowedInputFiles.length != 0) {
            if (Arrays.asList(featureSet.allowedInputFiles).contains("/")) {
                unrestrictedInput = true;
                allowedInputPaths = null;
            } else {
                unrestrictedInput = false;
                allowedInputPaths = toPaths(featureSet.allowedInputFiles);
            }
            env |= 2;
        } else {
            unrestrictedInput = false;
            allowedInputPaths = null;
        }
        if (featureSet.allowedOutputFiles != null && featureSet.allowedOutputFiles.length != 0) {
            if (Arrays.asList(featureSet.allowedOutputFiles).contains("/")) {
                unrestrictedOutput = true;
                allowedOutputPaths = null;
            } else {
                unrestrictedOutput = false;
                allowedOutputPaths = toPaths(featureSet.allowedOutputFiles);
            }
            env |= 4;
        } else {
            unrestrictedOutput = false;
            allowedOutputPaths = null;
        }
        if (featureSet.sysCall) {
            env |= 8;
        }
        envFlags = env;

        if (!featureSet.perl) {
            fingerprintBlackList.add(PERL.INSTANCE.code());
        }
    }

    @SneakyThrows
    private static Path[] toPaths(String[] files) {
        List<Path> list = new ArrayList<>();
        for (String file : files) {
            list.add(Paths.get(file).toRealPath());
        }
        return list.toArray(new Path[0]);
    }

    public static int executeProgram(String[] args, byte[] program, InputStream input, OutputStream output, FileIOSupplier fileIOSupplier, FeatureSet featureSet) {
        val interpreter = new Interpreter(args, input, output, fileIOSupplier, featureSet);
        interpreter.fungeSpace().loadFileAt(0, 0, 0, program, featureSet.trefunge);
        //Init step
        {
            val ip = interpreter.IPs.get(0);
            ip.position().sub(ip.delta());
            interpreter.step(ip);
        }
        if (featureSet.maxIter > 0) {
            long step = 0;
            while (!interpreter.stopped() && step < featureSet.maxIter) {
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
    public IP[] allIPs() {
        return IPs.toArray(new IP[0]);
    }

    @Override
    public IP IP() {
        return currentIP;
    }

    @Override
    public IP cloneIP() {
        clone = currentIP.deepCopy();
        clone.UUID(nextUUID++);
        return clone;
    }

    private Map<String, Object> getMap(int key) {
        if (globals.containsKey(key)) {
            return globals.get(key);
        } else {
            val map = new HashMap<String, Object>();
            globals.put(key, map);
            return map;
        }
    }

    @Override
    public <T> T getGlobal(int finger, String key) {
        return (T) getMap(finger).getOrDefault(key, null);
    }

    @Override
    public <T> void putGlobal(int finger, String key, T value) {
        getMap(finger).put(key, value);
    }

    @Override
    public boolean hasGlobal(int finger, String key) {
        return getMap(finger).containsKey(key);
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
            IP().stringMode(!IP().stringMode());
        } else if (IP().stringMode()) {
            IP().stackStack().TOSS().push(opcode);
        } else {
            Instruction instr;
            if ((instr = IP().instructionManager().fetch(opcode)) != null || (instr = baseInstructionManager.fetch(opcode)) != null) {
                instr.process(this);
            } else {
                if (opcode == 'r')
                    throw new IllegalArgumentException("Language does not implement 'r' reflect instruction.");
                interpret('r');
            }
        }
    }


    private boolean wrappingStep(IP ip) {
        ip.step();
        if (!fungeSpace().bounds().inBounds(ip.position())) {
            ip.reflect();
            do {
                ip.step();
            } while (!fungeSpace().bounds().inBounds(ip.position()));
            do {
                ip.step();
            } while (fungeSpace().bounds().inBounds(ip.position()));
            ip.reflect();
            do {
                ip.step();
            } while (!fungeSpace().bounds().inBounds(ip.position()));
            return true;
        }
        return false;
    }

    public void step(IP ip) {
        int p;
        if (ip.stringMode()) {
            p = fungeSpace.get(ip.position());
            if (p != ' ') {
                wrappingStep(ip);
                return;
            }
        }
        int flipCount = 0;
        do {
            flipCount += wrappingStep(ip) ? 1 : 0;
            p = fungeSpace.get(ip.position());
            if (!ip.stringMode()) {
                while (p == ';') {
                    do {
                        flipCount += wrappingStep(ip) ? 1 : 0;
                        p = fungeSpace.get(ip.position());
                    } while (p != ';');
                    flipCount += wrappingStep(ip) ? 1 : 0;
                    p = fungeSpace.get(ip.position());
                }
            }
            if (flipCount == 1000) {
                throw new IllegalStateException("IP " + ip.UUID() + " is stuck on a blank strip!\nPos: " + ip.position() + ", Delta: " + ip.delta() + (ip.position().equals(0, 0, 0) && ip.UUID() == 0 ? "\nIs the file empty?" : ""));
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

    @Override
    public boolean fileInputAllowed(String file) throws IOException {
        if ((envFlags & 0x02) == 0) {
            return false;
        }
        if (unrestrictedInput) return true;
        val path = Paths.get(file).toRealPath();
        return Arrays.stream(allowedInputPaths).anyMatch(path::startsWith);
    }

    @Override
    public boolean fileOutputAllowed(String file) throws IOException {
        if ((envFlags & 0x04) == 0) {
            return false;
        }
        if (unrestrictedOutput) return true;
        val path = Paths.get(file).toRealPath();
        return Arrays.stream(allowedOutputPaths).anyMatch(path::startsWith);
    }

    @Override
    public boolean syscallAllowed() {
        return (envFlags & 0x08) != 0;
    }

    @Override
    public boolean fingerprintAllowed(int code) {
        return !fingerprintBlackList.contains(code);
    }

    public void tick() {
        currentIP = null;
        for (int i = 0; i < IPs.size(); i++) {
            currentIP = IPs.get(i);
            if (IP().dead()) {
                IPs.remove(i);
                i--;
                continue;
            }
            interpret(fungeSpace().get(IP().position()));
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
