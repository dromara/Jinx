
package com.happylifeplat.netty.session;



public interface ServletNettyHttpSessionStore {

    NettyHttpSession findSession(String sessionId);

    NettyHttpSession createSession();

    void destroySession(String sessionId);

    void destroyInactiveSessions();
}
