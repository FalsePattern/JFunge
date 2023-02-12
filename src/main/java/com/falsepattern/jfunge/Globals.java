package com.falsepattern.jfunge;

public class Globals {
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;
    public static final int HANDPRINT = 0x74_70_85_78; //"JFUN"
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCH_VERSION;
    public static final int FUNGE_VERSION = MAJOR_VERSION * 256 * 256 + MINOR_VERSION * 256 + PATCH_VERSION;
}
