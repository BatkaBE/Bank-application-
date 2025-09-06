package com.golomt.loan.GMTUtility;

import com.golomt.loan.GMTConstant.GMTLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GMTLOGUtilities {

    public static void info(String service, String message) {
        log.info("[{}] {}", service, message);
    }

    public static void debug(String service, String message) {
        log.debug("[{}] {}", service, message);
    }

    public static void error(String service, String message) {
        log.error("[{}] {}", service, message);
    }

    public static void warn(String service, String message) {
        log.warn("[{}] {}", service, message);
    }
}
