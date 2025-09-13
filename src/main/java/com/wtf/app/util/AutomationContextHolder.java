package com.wtf.app.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.wtf.app.model.dto.AutomationContext;

public class AutomationContextHolder {

    private static final TransmittableThreadLocal<AutomationContext> contextHolder = new TransmittableThreadLocal<>();

    public static void setContext(AutomationContext context) {
        contextHolder.set(context);
    }

    public static AutomationContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
