
package com.happylifeplat.netty.session;

import com.happylifeplat.netty.utils.Utils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyHttpSession implements HttpSession {

    public static final String  SESSION_ID_KEY      = "JSESSIONID";

    private String              id;

    private long                creationTime;

    private long                lastAccessedTime;

    private int                 maxInactiveInterval = -1;

    private Map<String, Object> attributes;

    public NettyHttpSession(String id) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return Utils.enumerationFromKeys(attributes);
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
        // return NettyEmbeddedContext.get();
    }

    @Override
    public HttpSessionContext getSessionContext() {
        throw new IllegalStateException("As of Version 2.1, this method is deprecated and has no replacement.");
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public String[] getValueNames() {
        if (attributes == null) return null;

        return attributes.keySet().toArray(new String[attributes.keySet().size()]);
    }

    @Override
    public void invalidate() {
        if (attributes != null) {
            attributes.clear();
        }
    }

    @Override
    public void putValue(String name, Object value) {
        this.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        if (attributes != null) {
            Object value = attributes.get(name);
            if (value != null && value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
            attributes.remove(name);
        }
    }

    @Override
    public void removeValue(String name) {
        this.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (attributes == null) attributes = new ConcurrentHashMap<String, Object>();

        attributes.put(name, value);

        if (value != null && value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
        }

    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;

    }

    public void touch() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    @Override
    public boolean isNew() {
        throw new IllegalStateException("Method 'isNew' not yet implemented!");
    }
}
