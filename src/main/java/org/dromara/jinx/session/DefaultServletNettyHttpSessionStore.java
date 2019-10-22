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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaoyu
 */
public class DefaultServletNettyHttpSessionStore implements ServletNettyHttpSessionStore {

    private static final Logger log = LoggerFactory.getLogger(DefaultServletNettyHttpSessionStore.class);

    public static ConcurrentHashMap<String, NettyHttpSession> sessions = new ConcurrentHashMap<String, NettyHttpSession>();

    @Override
    public NettyHttpSession createSession() {
        String sessionId = this.generateNewSessionId();
        log.debug("Creating new session with id {}", sessionId);

        NettyHttpSession session = new NettyHttpSession(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    @Override
    public void destroySession(String sessionId) {
        log.debug("Destroying session with id {}", sessionId);
        sessions.remove(sessionId);
    }

    @Override
    public NettyHttpSession findSession(String sessionId) {
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }

    protected String generateNewSessionId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void destroyInactiveSessions() {
        for (Map.Entry<String, NettyHttpSession> entry : sessions.entrySet()) {
            NettyHttpSession session = entry.getValue();
            if (session.getMaxInactiveInterval() < 0) continue;

            long currentMillis = System.currentTimeMillis();

            if (currentMillis - session.getLastAccessedTime() > session.getMaxInactiveInterval() * 1000) {
                destroySession(entry.getKey());
            }
        }
    }
}
