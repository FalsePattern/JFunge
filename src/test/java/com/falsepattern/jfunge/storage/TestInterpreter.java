package com.falsepattern.jfunge.storage;

import com.falsepattern.jfunge.interpreter.FeatureSet;
import com.falsepattern.jfunge.interpreter.Interpreter;
import lombok.val;
import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TestInterpreter {
    private static final Interpreter.FileIOSupplier fakeSupplier = new Interpreter.FileIOSupplier() {

        private Path currentDirectory = Paths.get("/");

        private final Map<Path, byte[]> files;

        {
            files = new HashMap<>();
            files.put(Paths.get("/"), null);
        }

        @Override
        public byte[] readFile(String name) throws IOException {
            val file = toRealPath(name);
            if (files.containsKey(file)) {
                val b = files.get(file);
                if (b == null) {
                    return null;
                }
                return Arrays.copyOf(b, b.length);
            } else {
                try (val s = TestInterpreter.class.getResourceAsStream(file.toString())) {
                    if (s == null) {
                        throw new FileNotFoundException("Could not find resource " + file);
                    }
                    val ret = new ByteArrayOutputStream();
                    val b = new byte[4096];
                    var r = 0;
                    while ((r = s.read(b)) > 0) {
                        ret.write(b, 0, r);
                    }
                    val bytes = ret.toByteArray();
                    files.put(file, bytes);
                    return Arrays.copyOf(bytes, bytes.length);
                }
            }
        }

        @Override
        public boolean writeFile(String file, byte[] data) {
            val path = toRealPath(file);
            if (files.containsKey(path)) {
                if (files.get(path) == null) {
                    return false;
                }
            }
            files.put(path, Arrays.copyOf(data, data.length));
            return true;
        }

        @Override
        public Path toRealPath(String file) {
            var path = Paths.get(file);
            if (path.startsWith("/")) {
                return path;
            }
            if (!path.isAbsolute()) {
                path = currentDirectory.resolve(path);
            }
            return path.normalize();
        }

        @Override
        public boolean changeDirectory(String dir) {
            val path = toRealPath(dir);
            if (files.containsKey(path)) {
                if (files.get(path) == null) {
                    currentDirectory = path;
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean createDirectory(String dir) {
            val path = toRealPath(dir);
            if (files.containsKey(path)) {
                return false;
            }
            files.put(path, null);
            return true;
        }

        @Override
        public boolean deleteDirectory(String dir) {
            val path = toRealPath(dir);
            if (files.containsKey(path)) {
                if (files.get(path) == null) {
                    files.remove(path);
                    return true;
                }
            }
            return false;
        }
    };

    @SuppressWarnings("SameParameterValue")
    private static byte[] readProgram(String path) {
        val program = new ByteArrayOutputStream();
        Assertions.assertDoesNotThrow(() -> {
            val reader = TestInterpreter.class.getResourceAsStream(path);
            Assertions.assertNotNull(reader);
            var read = 0;
            val b = new byte[4096];
            while ((read = reader.read(b)) > 0) {
                program.write(b, 0, read);
            }
        });
        return program.toByteArray();
    }

    private static int interpret(String[] args, byte[] code, InputStream input, OutputStream output, FeatureSet featureSet) {
        return Assertions.assertDoesNotThrow(() -> Interpreter.executeProgram(args, code, input, output, fakeSupplier, featureSet));
    }

    private static InputStream nullStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static void execMycoProgram(String program, int expectedReturnCode, FeatureSet featureSet, String input) {
        val checkingOutput = new ByteArrayOutputStream();
        val output = new TeeOutputStream(checkingOutput, System.out);
        val returnCode = interpret(new String[]{program}, readProgram("/" + program), new ByteArrayInputStream(input.getBytes()), output, featureSet);
        val txt = checkingOutput.toString();
        String currentlyActiveFingerprint = null;
        boolean fingerprintHadError = false;
        boolean good = true;
        val implementedFingerprints = new HashSet<String>();
        val unimplementedFingerprints = new HashSet<String>();
        for (val line: txt.split("\n")) {
            if (line.startsWith("Testing fingerprint ")) {
                int start = "Testing fingerprint ".length();
                currentlyActiveFingerprint = line.substring(start, start + 4);
                if (line.endsWith("not loaded.")) {
                    unimplementedFingerprints.add(currentlyActiveFingerprint);
                } else {
                    implementedFingerprints.add(currentlyActiveFingerprint);
                }
                fingerprintHadError = false;
            } else if (line.equals("About to test detailed () behaviour with two fingerprints.")) {
                //Fingerprint core checks are over, stop tracking.
                currentlyActiveFingerprint = null;
                fingerprintHadError = false;
            }
            if (line.startsWith("BAD")) {
                if (good) {
                    System.err.println("Found BAD check(s) in Mycology! Interpreter is NOT standard-compliant.");
                    good = false;
                }
                if (currentlyActiveFingerprint != null) {
                    if (!fingerprintHadError) {
                        System.err.println("Broken fingerprint: " + currentlyActiveFingerprint);
                        fingerprintHadError = true;
                    }
                } else {
                    System.err.println("Not inside a fingerprint test, base language spec is broken. Fix urgently!");
                }
                System.err.print("    ");
                System.err.println(line);
            }
        }
        System.out.println("Implemented fingerprints: ");
        for (val fingerprint: implementedFingerprints) {
            System.out.print("    ");
            System.out.println(fingerprint);
        }
        System.out.println("Unimplemented fingerprints: ");
        for (val fingerprint: unimplementedFingerprints) {
            System.out.print("    ");
            System.out.println(fingerprint);
        }
        Assertions.assertTrue(good);
        Assertions.assertEquals(expectedReturnCode, returnCode);
    }

    @Test
    public void testMycology() {
        val featureSet = FeatureSet.builder()
                                   .allowedInputFiles(new String[]{"/"})
                                   .allowedOutputFiles(new String[]{"/"})
                                   .sysCall(false)
                                   .concurrent(true)
                                   .environment(false)
                                   .perl(true)
                                   .maxIter(300000L)
                                   .build();
        execMycoProgram("mycology.b98", 15, featureSet, "");
    }

    @Test
    public void testMycoUser() {
        val featureSet = FeatureSet.builder()
                                   .allowedInputFiles(new String[]{"/"})
                                   .allowedOutputFiles(new String[]{"/"})
                                   .sysCall(false)
                                   .concurrent(true)
                                   .environment(false)
                                   .perl(true)
                                   .maxIter(300000L)
                                   .build();
        execMycoProgram("mycouser.b98", 0, featureSet, "123\nt\n16\nf0f0\n");
    }

    @Test
    public void testSemicolonAtStart() {
        System.out.println("Testing edge case ;;.@");
        val output = new ByteArrayOutputStream();
        val returnCode = interpret(new String[0], ";;.@".getBytes(StandardCharsets.UTF_8), nullStream(), output, FeatureSet.builder().maxIter(50).build());
        val txt = output.toString();
        Assertions.assertEquals("0 ", txt);
        Assertions.assertEquals(0, returnCode);
    }

    @Test
    public void testPutCharAtStart() {
        System.out.println("Testing edge case 'a,@");
        val output = new ByteArrayOutputStream();
        val returnCode = interpret(new String[0], "'a,@".getBytes(StandardCharsets.UTF_8), nullStream(), output, FeatureSet.builder().maxIter(50).build());
        val txt = output.toString();
        Assertions.assertEquals("a", txt);
        Assertions.assertEquals(0, returnCode);
    }
}
