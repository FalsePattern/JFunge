package com.falsepattern.jfunge.storage;

import javassist.ClassPool;
import javassist.CtMethod;
import lombok.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class AsWrittenMethodOrderer implements MethodOrderer {
    @Override
    public void orderMethods(MethodOrdererContext context) {
        val orderedMethodNames = orderedMethodNames(context);
        context.getMethodDescriptors()
               .sort(Comparator.comparingInt(o -> orderedMethodNames.indexOf(o.getMethod().getName())));
    }

    @SneakyThrows
    protected List<String> orderedMethodNames(@NonNull MethodOrdererContext context) {
        return Arrays.stream(ClassPool.getDefault().get(context.getTestClass().getName()).getDeclaredMethods())
                     .map(CtMethod::getName)
                     .collect(Collectors.toList());
    }
}