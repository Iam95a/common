package com.chen.common.im;

import org.springframework.context.ApplicationContext;

public class AppContext {
    public static ApplicationContext context;

    public static void init(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
