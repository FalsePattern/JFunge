package com.falsepattern.jfunge.interpreter.instructions;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import lombok.val;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

class InstructionFactory extends ClassLoader {
    private static final InstructionFactory INSTANCE = new InstructionFactory();
    private final Map<Method, Instruction> proxies = new HashMap<>();
    private final Map<String, Class<?>> nameToClass = new HashMap<>();

    private InstructionFactory() {}

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (nameToClass.containsKey(name)) {
            return nameToClass.get(name);
        }
        return super.findClass(name);
    }

    private static final AtomicLong counter = new AtomicLong();

    static Instruction createInstruction(Method method) {
        return INSTANCE.createInstructionImpl(method);
    }

    private Instruction createInstructionImpl(Method method) {
        if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("createInstruction only works with public static methods! " + method.getDeclaringClass().getName() + "." + method.getName() + " is not an interface!");
        }
        if (!proxies.containsKey(method)) {
            defineInstructionProxy(method);
        }
        return proxies.get(method);
    }

    private static final String[] INTERFACES_INTERNAL_NAME;
    private static final String DESCRIPTOR;

    static {
        try {
            INTERFACES_INTERNAL_NAME = new String[]{Type.getInternalName(Instruction.class)};
            DESCRIPTOR = Type.getMethodDescriptor(Instruction.class.getMethod("process", ExecutionContext.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void defineInstructionProxy(Method targetMethod) {
        try {
            val declClass = targetMethod.getDeclaringClass();
            val newClassName = "com/falsepattern/jfunge/interpreter/instructions/proxy/" + (declClass.getName() + "." + targetMethod.getName()).replace('.', '_').replace('$', '_') + "_" + counter.incrementAndGet();
            val writer = new ClassWriter(0);
            writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, newClassName, null, "java/lang/Object", INTERFACES_INTERNAL_NAME);
            val method = writer.visitMethod(Opcodes.ACC_PUBLIC, "process", DESCRIPTOR, null, null);
            method.visitVarInsn(Opcodes.ALOAD, 1);
            method.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(declClass), targetMethod.getName(), DESCRIPTOR, false);
            method.visitInsn(Opcodes.RETURN);
            method.visitMaxs(1, 2);
            method.visitEnd();
            val initMethod = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            initMethod.visitVarInsn(Opcodes.ALOAD, 0);
            initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            initMethod.visitInsn(Opcodes.RETURN);
            initMethod.visitMaxs(1, 1);
            initMethod.visitEnd();
            writer.visitEnd();
            var bytes = writer.toByteArray();
            var name = newClassName.replace('/', '.');
            var cl = (Class<? extends Instruction>) defineClass(name, bytes, 0, bytes.length);
            var constructor = cl.getConstructor();
            proxies.put(targetMethod, constructor.newInstance());
            nameToClass.put(name, cl);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
