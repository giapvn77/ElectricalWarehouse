package com.electricalwarehouse.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Human-readable document codes, e.g. PN-261028-153012-K7Q. Unique column guards collisions. */
public final class CodeGenerator {

    public static final String STOCK_IN = "PN";
    public static final String STOCK_OUT = "PX";
    public static final String TRANSFER = "CK";
    public static final String ADJUSTMENT = "KK";
    public static final String DEFECT = "HL";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyMMdd-HHmmss");
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeGenerator() {
    }

    public static String next(String prefix) {
        StringBuilder suffix = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            suffix.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return prefix + "-" + LocalDateTime.now().format(FORMAT) + "-" + suffix;
    }
}
