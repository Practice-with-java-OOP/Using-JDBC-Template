package com.jidian.cosalon.migration.pos365;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static String SESSION_ID = "";
    public static String PID = "";

    public static String PHONE_NUM = "00000000000";

    public static final long POS365_DOCUMENT_SELL = 1L;
    public static final long POS365_DOCUMENT_IMPORT_REVISE = 2L;
    public static final long POS365_DOCUMENT_RETURN = 3L;
    public static final long POS365_DOCUMENT_INVENTORY = 5L; // kiem ke
    public static final long POS365_DOCUMENT_MOVE_OUT = 6L;
    public static final long POS365_DOCUMENT_MOVE_IN = 13L;
    public static final List<Long> POS365_IMPORT = Arrays.asList(POS365_DOCUMENT_IMPORT_REVISE, POS365_DOCUMENT_RETURN, POS365_DOCUMENT_MOVE_IN);
    public static final List<Long> POS365_EXPORT = Arrays.asList(POS365_DOCUMENT_SELL, POS365_DOCUMENT_INVENTORY, POS365_DOCUMENT_MOVE_OUT);

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

    public static String normalize(String src) {
        if (src == null) return "";
        return src.replaceAll(" ", "").trim();
    }

    public static boolean isBlank(String src) {
        return src == null || src.trim().isEmpty();
    }
}
