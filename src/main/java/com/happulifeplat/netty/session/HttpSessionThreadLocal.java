/*
 * Copyright 2015-2020 msun.com All right reserved.
 */
package com.happulifeplat.netty.session;


public class HttpSessionThreadLocal {

    public static final ThreadLocal<NettyHttpSession> sessionThreadLocal = new ThreadLocal<>();

    private static ServletNettyHttpSessionStore       sessionStore;

    public static ServletNettyHttpSessionStore getSessionStore() {
        return sessionStore;
    }

    public static void setSessionStore(ServletNettyHttpSessionStore store) {
        sessionStore = store;
    }

    public static void set(NettyHttpSession session) {
        sessionThreadLocal.set(session);
    }

    public static void unset() {
        sessionThreadLocal.remove();
    }

    public static NettyHttpSession get() {
        NettyHttpSession session = sessionThreadLocal.get();
        if (session != null) session.touch();
        return session;
    }

    public static NettyHttpSession getOrCreate() {
        if (HttpSessionThreadLocal.get() == null) {
            if (sessionStore == null) {
                sessionStore = new DefaultServletNettyHttpSessionStore();
            }

            NettyHttpSession newSession = sessionStore.createSession();
            newSession.setMaxInactiveInterval(60 * 60);
            sessionThreadLocal.set(sessionStore.createSession());
        }
        return get();
    }
}
