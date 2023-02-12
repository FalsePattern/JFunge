package com.falsepattern.jfunge.interpreter;

import com.falsepattern.jfunge.ip.IP;
import com.falsepattern.jfunge.ip.IStack;
import com.falsepattern.jfunge.storage.FungeSpace;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface ExecutionContext {
    IP[] allIPs();

    IP IP();

    IP cloneIP();

    FungeSpace fungeSpace();

    <T> T getGlobal(int finger, String key);

    <T> void putGlobal(int finger, String key, T value);

    boolean hasGlobal(int finger, String key);

    int dimensions();

    boolean stopped();

    void stop(int exitCode);

    int exitCode();

    void interpret(int code);

    void step(IP ip);

    List<String> args();

    Map<String, String> env();

    List<String> envKeys();

    int input(boolean stagger);

    OutputStream output();

    byte[] readFile(String file) throws PermissionException;

    boolean writeFile(String file, byte[] data) throws PermissionException;

    int envFlags();

    boolean changeDirectory(String dir) throws PermissionException;

    boolean makeDirectory(String dir) throws PermissionException;

    boolean removeDirectory(String dir) throws PermissionException;

    default boolean concurrentAllowed() {
        return (envFlags() & 0x01) != 0;
    }

    default boolean syscallAllowed() {
        return (envFlags() & 0x08) != 0;
    }

    boolean fingerprintAllowed(int code);

    //Common shorthands

    default IStack stack() {
        return IP().stackStack().TOSS();
    }
}
