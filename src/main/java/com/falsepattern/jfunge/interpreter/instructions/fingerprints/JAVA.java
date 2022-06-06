package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class JAVA implements Fingerprint {
    public static final JAVA INSTANCE = new JAVA();
    private int classCounter = 0;
    private final Map<Integer, Class<?>> classMap = new HashMap<>();
    private int methodCounter = 0;
    private final Map<Integer, Method> methodMap = new HashMap<>();
    private int objectCounter = 0;
    private final Map<Integer, Object> objectMap = new HashMap<>();

    private JAVA() {
        objectMap.put(objectCounter++, null);
    }

    @Instr('C')
    public static void classForName(ExecutionContext ctx) {
        val className = ctx.IP().stackStack.TOSS().popString();
        try {
            val clazz = Class.forName(className);
            val existing = INSTANCE.classMap.entrySet().stream().filter((entry) -> entry.getValue().equals(clazz)).findAny();
            int id;
            if (existing.isPresent()) {
               id = existing.get().getKey();
            } else {
                id = INSTANCE.classCounter++;
                INSTANCE.classMap.put(id, clazz);
            }
            ctx.IP().stackStack.TOSS().push(id);
        } catch (ClassNotFoundException e) {
            ctx.interpret('r');
        }
    }

    @Instr('M')
    public static void getMethod(ExecutionContext ctx) {
        try {
            val methodFootprint = ctx.IP().stackStack.TOSS().popString();
            val methodBase = methodFootprint.substring(0, methodFootprint.indexOf('('));
            val methodName = methodBase.substring(methodBase.lastIndexOf('.') + 1);
            val args = descriptorsToListOfClasses(methodFootprint.substring(methodFootprint.indexOf('(') + 1, methodFootprint.indexOf(')')));
            val clazz = INSTANCE.classMap.get(ctx.IP().stackStack.TOSS().pop());
            val method = clazz.getMethod(methodName, args.toArray(new Class[0]));
            val existing = INSTANCE.methodMap.entrySet().stream().filter((entry) -> entry.getValue().equals(clazz)).findAny();
            int id;
            if (existing.isPresent()) {
                id = existing.get().getKey();
            } else {
                id = INSTANCE.methodCounter++;
                INSTANCE.methodMap.put(id, method);
            }
            ctx.IP().stackStack.TOSS().push(id);
        } catch (Exception e) {
            ctx.interpret('r');
        }
    }

    @Instr('I')
    public static void invoke(ExecutionContext ctx) {
        val s = ctx.IP().stackStack.TOSS();
        val method = INSTANCE.methodMap.get(s.pop());
        val object = INSTANCE.objectMap.get(s.pop());
        val argc = method.getParameterCount();
        val args = new Object[argc];
        for (int i = 0; i < args.length; i++) {
            args[i] = INSTANCE.objectMap.get(s.pop());
        }
        try {
            val ret = method.invoke(object, args);
            if (ret == null) {
                s.push(0);
            } else {
                s.push(putObject(ret));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            ctx.interpret('r');
        }
    }

    @Instr('F')
    public static void getField(ExecutionContext ctx) {
        try {
            val s = ctx.IP().stackStack.TOSS();
            val fieldName = s.popString();
            val clazz = INSTANCE.classMap.get(s.pop());
            val field = clazz.getField(fieldName);
            val obj = INSTANCE.objectMap.get(s.pop());
            s.push(putObject(field.get(obj)));
        } catch (Exception e) {
            ctx.interpret('r');
        }
    }

    @Instr('P')
    public static void parseString(ExecutionContext ctx) {
        val o = ctx.IP().stackStack.TOSS().popString().intern();
        val x = INSTANCE.objectMap.entrySet().stream().filter(o::equals).findFirst();
        if (x.isPresent()) {
            ctx.IP().stackStack.TOSS().push(x.get().getKey());
        } else {
            ctx.IP().stackStack.TOSS().push(putObject(o));
        }
    }

    private static int putObject(Object obj) {
        val x = INSTANCE.objectMap.entrySet().stream().filter((o) -> o == obj).findFirst();
        if (x.isPresent()) {
            return x.get().getKey();
        } else {
            val id = INSTANCE.objectCounter++;
            INSTANCE.objectMap.put(id, obj);
            return id;
        }
    }

    private static List<Class<?>> descriptorsToListOfClasses(String descriptor) throws ClassNotFoundException {
        val result = new ArrayList<Class<?>>();
        val argIter = descriptor.chars().iterator();
        StringBuilder currentClassName = null;
        while (argIter.hasNext()) {
            val c = argIter.nextInt();
            if (currentClassName != null) {
                if (c == ';') {
                    val className = currentClassName.toString();
                    currentClassName = null;
                    result.add(Class.forName(className));
                } else {
                    currentClassName.append(c == '/' ? '.' : (char)c);
                }
            } else {
                switch (c) {
                    case 'B':
                        result.add(byte.class);
                        break;
                    case 'C':
                        result.add(char.class);
                        break;
                    case 'D':
                        result.add(double.class);
                        break;
                    case 'F':
                        result.add(float.class);
                        break;
                    case 'I':
                        result.add(int.class);
                        break;
                    case 'J':
                        result.add(long.class);
                        break;
                    case 'L':
                        currentClassName = new StringBuilder();
                        break;
                    case 'S':
                        result.add(short.class);
                        break;
                    case 'Z':
                        result.add(boolean.class);
                        break;
                    case '[':
                        result.add(Array.newInstance(result.remove(result.size() - 1), 0).getClass());
                        break;
                }
            }
        }
        return result;
    }

    @Override
    public int code() {
        return 0x4a415641;
    }
}
