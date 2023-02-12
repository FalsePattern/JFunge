package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.interpreter.instructions.Funge98;
import com.falsepattern.jfunge.interpreter.instructions.Instruction;
import com.falsepattern.jfunge.interpreter.instructions.InstructionManager;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.PERL;
import com.falsepattern.jfunge.interpreter.instructions.fingerprints.SOCK;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
public class Interpreter implements ExecutionContext {
    public static final FileIOSupplier DEFAULT_FILE_IO_SUPPLIER = new FileIOSupplier() {
        private Path currentDirectory = Paths.get(System.getProperty("user.dir"));
        @Override
        public byte[] readFile(String file) throws IOException {
            return Files.readAllBytes(Paths.get(file));
        }

        @Override
        public boolean writeFile(String file, byte[] data) throws IOException {
            Files.write(Paths.get(file), data);
            return true;
        }

        @Override
        public Path toRealPath(String file) {
            var path = Paths.get(file);
            if (!path.isAbsolute()) {
                path = currentDirectory.resolve(path);
            }
            try {
                return path.toRealPath();
            } catch (IOException e) {
                return path.normalize();
            }
        }

        @Override
        public boolean changeDirectory(String dir) {
            val path = toRealPath(dir);
            if (Files.isDirectory(path)) {
                currentDirectory = path;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean createDirectory(String dir) {
            val path = toRealPath(dir);
            if (Files.exists(path)) {
                return false;
            } else {
                try {
                    Files.createDirectory(path);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }

        @Override
        public boolean deleteDirectory(String dir) {
            val path = toRealPath(dir);
            if (Files.isDirectory(path)) {
                try {
                    Files.delete(path);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
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

    @Getter
    private final Map<String, String> env;

    @Getter
    private final List<String> envKeys;

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

        if (featureSet.environment) {
            this.env = new HashMap<>(System.getenv());
        } else {
            this.env = new HashMap<>();
        }
        this.env.put("JFUNGE_ENV", featureSet.environment ? "PASS" : "BLOCK");
        envKeys = new ArrayList<>();
        envKeys.addAll(this.env.keySet());

        if (!featureSet.perl) {
            fingerprintBlackList.add(PERL.INSTANCE.code());
        }

        if (!featureSet.socket) {
            fingerprintBlackList.add(SOCK.INSTANCE.code());
            fingerprintBlackList.add(SOCK.SCKE.INSTANCE.code());
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
                IP().reflect();
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
    public int input(boolean stagger) {
        var value = -1;
        if (inputStagger > 0) {
            value = inputStagger;
            inputStagger = -1;
        } else {
            try {
                try {
                    output.flush();
                } catch (IOException ignored) {
                }
                value = input.read();
            } catch (IOException ignored) {}
        }
        if (stagger) {
            inputStagger = value;
        }
        return value;
    }

    @Override
    public byte[] readFile(String file) throws PermissionException {
        if (fileInputBlocked(file)) {
            throw new PermissionException("Cannot read file " + file + " (input not allowed).");
        }
        try {
            return fileIOSupplier.readFile(file);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean writeFile(String file, byte[] data) throws PermissionException {
        if (fileOutputBlocked(file)) {
            throw new PermissionException("Cannot write to file " + file + " (output not allowed).");
        }
        try {
            return fileIOSupplier.writeFile(file, data);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean changeDirectory(String dir) throws PermissionException {
        if (fileInputBlocked(dir)) {
            throw new PermissionException("Cannot change directory to " + dir + " (input not allowed).");
        }
        return fileIOSupplier.changeDirectory(dir);
    }

    @Override
    public boolean makeDirectory(String dir) throws PermissionException {
        if (fileOutputBlocked(dir)) {
            throw new PermissionException("Cannot create directory " + dir + " (output not allowed).");
        }
        return fileIOSupplier.createDirectory(dir);
    }

    @Override
    public boolean removeDirectory(String dir) throws PermissionException {
        if (fileOutputBlocked(dir)) {
            throw new PermissionException("Cannot delete directory " + dir + " (output not allowed).");
        }
        return fileIOSupplier.deleteDirectory(dir);
    }

    private boolean fileInputBlocked(String file) {
        if ((envFlags & 0x02) == 0) {
            return true;
        }
        if (unrestrictedInput) return false;
        val path = fileIOSupplier.toRealPath(file);
        return Arrays.stream(allowedInputPaths).noneMatch(path::startsWith);
    }

    private boolean fileOutputBlocked(String file) {
        if ((envFlags & 0x04) == 0) {
            return true;
        }
        if (unrestrictedOutput) return false;
        val path = fileIOSupplier.toRealPath(file);
        return Arrays.stream(allowedOutputPaths).noneMatch(path::startsWith);
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
            if (!IP().dead()) {
                interpret(fungeSpace().get(IP().position()));
            }
            if (IP().dead()) {
                IPs.remove(i);
                i--;
            }
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

        Path toRealPath(String file);

        boolean changeDirectory(String dir);

        boolean createDirectory(String dir);

        boolean deleteDirectory(String dir);
    }
}
