package com.jidian.cosalon.migration.pos365;

import java.math.BigDecimal;

public class Utils {
    public static String SESSION_ID = "";
    public static String PID = "";

    public static String PHONE_NUM = "00000000000";

    public static String nvl(String src) {
        return src == null ? "": src;
    }

    public static Long nvl(Long src) {
        return src == null ? 0L: src;
    }

    public static BigDecimal nvl(BigDecimal src) {
        return src == null ? BigDecimal.ZERO: src;
    }

    public static Integer nvl(Integer src) {
        return src == null ? 0: src;
    }
}
