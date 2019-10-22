/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * Contributor license agreements.See the NOTICE file distributed with
 * This work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * he License.You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.dromara.jinx.session;


/**
 * @author xiaoyu
 */
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
