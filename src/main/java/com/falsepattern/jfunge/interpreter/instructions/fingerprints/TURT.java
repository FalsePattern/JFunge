package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import com.falsepattern.jfunge.util.MemoryStack;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.val;
import org.joml.Math;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector2i;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TURT implements Fingerprint {
    public static final TURT INSTANCE = new TURT();

    private static BufferedImage image;
    private static Graphics2D gfx;

    private static Vector2i dimensions;
    private static Vector2f position;
    private static Color lineColor;
    private static boolean penDown;
    private static int angle;

    static {
        initialize(new Vector2i(500, 500), new Vector2i(0, 0));
    }

    public static void initialize(Vector2i dimensions, Vector2i initialPosition) {
        TURT.dimensions = new Vector2i(dimensions);
        TURT.position = new Vector2f(initialPosition);
        TURT.angle = 0;
        TURT.penDown = false;

        TURT.image = new BufferedImage(dimensions.x, dimensions.y, BufferedImage.TYPE_INT_RGB);
        TURT.gfx = image.createGraphics();
        gfx.setColor(Color.WHITE);
        gfx.fillRect(0, 0, dimensions.x, dimensions.y);
        gfx.setColor(lineColor = Color.BLACK);
    }

    private static void clampPos() {
        if (position.x < 0) {
            position.x = 0;
        }
        if (position.x > dimensions.x - 1) {
            position.x = dimensions.x - 1;
        }
        if (position.y < 0) {
            position.y = 0;
        }
        if (position.y > dimensions.y - 1) {
            position.y = dimensions.y - 1;
        }
    }

    @Instr('L')
    public static void turnLeft(ExecutionContext ctx) {
        angle = (((angle + ctx.stack().pop()) % 360) + 360) % 360;
    }

    @Instr('R')
    public static void turnRight(ExecutionContext ctx) {
        angle = (((angle - ctx.stack().pop()) % 360) + 360) % 360;
    }

    @Instr('H')
    public static void setHeading(ExecutionContext ctx) {
        angle = ((ctx.stack().pop() % 360) + 360) % 360;
    }

    @Instr('F')
    public static void forward(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val newPos = mem.vec2f().set(1, 0);
        new Matrix3x2f().rotation(Math.toRadians(-angle)).transformDirection(newPos);
        val step = ctx.stack().pop();
        if (penDown) {
            newPos.mul(step).add(position);
            gfx.drawLine((int) position.x, (int) position.y, (int) newPos.x, (int) newPos.y);
            position.set(newPos);
        } else {
            position.add(newPos.mul(step));
        }
        clampPos();
    }

    @Instr('B')
    public static void back(ExecutionContext ctx) {
        @Cleanup val mem = MemoryStack.stackPush();
        val newPos = mem.vec2f().set(1, 0);
        new Matrix3x2f().rotation(Math.toRadians(-angle)).transformDirection(newPos);
        val step = ctx.stack().pop();
        if (penDown) {
            newPos.mul(-step).add(position);
            gfx.drawLine((int) position.x, (int) position.y, (int) newPos.x, (int) newPos.y);
            position.set(newPos);
        } else {
            position.add(newPos.mul(-step));
        }
        clampPos();
    }

    @Instr('P')
    public static void penPos(ExecutionContext ctx) {
        penDown = ctx.stack().pop() != 0;
    }

    @Instr('C')
    public static void penColor(ExecutionContext ctx) {
        val rgb = ctx.stack().pop() & 0xFFFFFF;
        lineColor = new Color(rgb);
        gfx.setColor(lineColor);
    }

    @Instr('N')
    public static void clearPaper(ExecutionContext ctx) {
        val rgb = ctx.stack().pop() & 0xFFFFFF;
        gfx.setColor(new Color(rgb));
        gfx.fillRect(0, 0, dimensions.x, dimensions.y);
        gfx.setColor(lineColor);
    }

    @Instr('D')
    public static void showDisplay(ExecutionContext ctx) {
        System.err.println("TODO: TURT 'D' unsupported. Popping stack, but not displaying.");
        ctx.stack().pop();
    }

    @Instr('T')
    public static void teleport(ExecutionContext ctx) {
        val stack = ctx.stack();
        position.y = stack.pop();
        position.x = stack.pop();
    }

    @Instr('E')
    public static void queryPen(ExecutionContext ctx) {
        ctx.stack().push(penDown ? 1 : 0);
    }

    @Instr('A')
    public static void queryHeading(ExecutionContext ctx) {
        ctx.stack().push(angle);
    }

    @Instr('Q')
    public static void queryPosition(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push((int) position.x);
        stack.push((int) position.y);
    }

    @Instr('U')
    public static void queryBounds(ExecutionContext ctx) {
        val stack = ctx.stack();
        stack.push(0);
        stack.push(0);
        stack.push(dimensions.x);
        stack.push(dimensions.y);
    }

    @Instr('I')
    public static void printDrawing(ExecutionContext ctx) {
        File file;
        int counter = 0;
        do {
            file = Paths.get(".", "turtle" + (counter++) + ".png").toFile();
        } while (file.exists());
        try {
            ImageIO.write(image, "PNG", file);
        } catch (IOException e) {
            e.printStackTrace();
            ctx.interpret('r');
        }
    }

    @Override
    public int code() {
        return 0x54555254;
    }
}
