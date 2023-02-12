package com.falsepattern.jfunge.interpreter.instructions.fingerprints;

import com.falsepattern.jfunge.interpreter.ExecutionContext;
import com.falsepattern.jfunge.interpreter.instructions.Fingerprint;
import lombok.NoArgsConstructor;
import lombok.val;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.JulianFields;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DATE implements Fingerprint {
    public static final DATE INSTANCE = new DATE();

    @Instr('A')
    public static void addDaysToDate(ExecutionContext ctx) {
        val stack = ctx.stack();
        val days = stack.pop();
        val d = stack.pop();
        val m = stack.pop();
        val y = stack.pop();
        if (y == 0) {
            ctx.IP().reflect();
            return;
        }
        val date = dateOrReflect(ctx, () -> LocalDate.of(y, m, d));
        if (date == null) {
            return;
        }
        val newDate = date.plusDays(days);
        stack.push(newDate.getYear());
        stack.push(newDate.getMonthValue());
        stack.push(newDate.getDayOfMonth());
    }

    @Instr('C')
    public static void julianDayToDate(ExecutionContext ctx) {
        val stack = ctx.stack();
        val jd = stack.pop();
        val date = dateOrReflect(ctx, () -> LocalDate.ofEpochDay(jd - 2440588));
        if (date == null) {
            return;
        }
        var y = date.getYear();
        if (y == 0) {
            ctx.IP().reflect();
            return;
        }
        if (y < 0) {
            y--;
        }
        stack.push(y);
        stack.push(date.getMonthValue());
        stack.push(date.getDayOfMonth());
    }

    @Instr('D')
    public static void daysBetweenDates(ExecutionContext ctx) {
        val stack = ctx.stack();
        val d2 = stack.pop();
        val m2 = stack.pop();
        val y2 = stack.pop();
        val d1 = stack.pop();
        val m1 = stack.pop();
        val y1 = stack.pop();
        if (y1 == 0 || y2 == 0) {
            ctx.IP().reflect();
            return;
        }
        val date1 = dateOrReflect(ctx, () -> LocalDate.of(y1, m1, d1));
        if (date1 == null) {
            return;
        }
        val date2 = dateOrReflect(ctx, () -> LocalDate.of(y2, m2, d2));
        if (date2 == null) {
            return;
        }
        stack.push((int) Duration.between(date2.atStartOfDay(), date1.atStartOfDay()).toDays());
    }

    @Instr('J')
    public static void dateToJulianDay(ExecutionContext ctx) {
        val stack = ctx.stack();
        val d = stack.pop();
        val m = stack.pop();
        var y = stack.pop();
        if (y == 0) {
            ctx.IP().reflect();
            return;
        }
        if (y < 0) {
            y++;
        }
        int finalY = y;
        val date = dateOrReflect(ctx, () -> LocalDate.of(finalY, m, d));
        if (date == null) {
            return;
        }
        val x = (int) JulianFields.JULIAN_DAY.getFrom(date);
        System.out.println(x);
        stack.push(x);
    }

    @Instr('T')
    public static void yearPlusDayToDate(ExecutionContext ctx) {
        val stack = ctx.stack();
        val day = stack.pop() + 1;
        val year = stack.pop();
        if (year == 0) {
            ctx.IP().reflect();
            return;
        }
        val date = dateOrReflect(ctx, () -> LocalDate.ofYearDay(year, day));
        if (date == null) {
            return;
        }
        stack.push(date.getYear());
        stack.push(date.getMonthValue());
        stack.push(date.getDayOfMonth());
    }

    @Instr('W')
    public static void dayOfWeek(ExecutionContext ctx) {
        val stack = ctx.stack();
        val d = stack.pop();
        val m = stack.pop();
        val y = stack.pop();
        if (y == 0) {
            ctx.IP().reflect();
            return;
        }
        val date = dateOrReflect(ctx, () -> LocalDate.of(y, m, d));
        if (date == null) {
            return;
        }
        stack.push(date.getDayOfWeek().getValue() - 1);
    }

    @Instr('Y')
    public static void dayOfYear(ExecutionContext ctx) {
        val stack = ctx.stack();
        val d = stack.pop();
        val m = stack.pop();
        val y = stack.pop();
        if (y == 0) {
            ctx.IP().reflect();
            return;
        }
        val date = dateOrReflect(ctx, () -> LocalDate.of(y, m, d));
        if (date == null) {
            return;
        }
        stack.push(date.getDayOfYear() - 1);
    }

    private static LocalDate dateOrReflect(ExecutionContext ctx, DateSupplier supplier) {
        try {
            return supplier.supply();
        } catch (DateTimeException e) {
            ctx.IP().reflect();
            return null;
        }
    }

    @Override
    public int code() {
        return 0x44415445;
    }

    private interface DateSupplier {
        LocalDate supply() throws DateTimeException;
    }
}
