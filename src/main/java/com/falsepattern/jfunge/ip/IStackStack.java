package com.falsepattern.jfunge.ip;

import com.falsepattern.jfunge.Copiable;

import java.util.Optional;

public interface IStackStack extends Copiable<IStackStack> {
    IStack TOSS();

    Optional<IStack> SOSS();

    boolean push();

    boolean pop();

    int size();

    int[] stackSizes();

    boolean invertMode();

    void invertMode(boolean state);

    boolean queueMode();

    void queueMode(boolean state);
}
