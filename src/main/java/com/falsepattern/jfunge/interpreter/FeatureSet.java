package com.falsepattern.jfunge.interpreter;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class FeatureSet {
    public final boolean trefunge;
    public final boolean sysCall;
    public final String[] allowedInputFiles;
    public final String[] allowedOutputFiles;
    public final boolean concurrent;
    public final boolean environment;
    public final long maxIter;

    public final boolean perl;
}
