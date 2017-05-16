
package com.happylifeplat.netty.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zxc Mar 14, 2016 5:16:48 PM
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
