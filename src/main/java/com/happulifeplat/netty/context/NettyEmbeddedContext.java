/*
 * Copyright 2015-2020 msun.com All right reserved.
 */
package com.happulifeplat.netty.context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.happulifeplat.netty.config.NettyContainerConfig;
import com.happulifeplat.netty.dispatcher.NettyRequestDispatcher;
import com.happulifeplat.netty.dispatcher.SimpleFilterChain;
import com.happulifeplat.netty.registration.NettyFilterRegistration;
import com.happulifeplat.netty.registration.NettyServletRegistration;
import com.happulifeplat.netty.utils.Utils;
import io.netty.util.AsciiString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.toList;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *  netty容器上下文
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
public class NettyEmbeddedContext implements ServletContext {

    private final Log logger = LogFactory.getLog(NettyEmbeddedContext.class);

    private final String contextPath;
    private final ClassLoader classLoader;
    private final Map<String, NettyServletRegistration> servlets = Maps.newConcurrentMap();
    private final Map<String, String> servletMappings =  Maps.newConcurrentMap();
    private final Map<String, NettyFilterRegistration> filters =  Maps.newConcurrentMap();
    private final AsciiString serverInfo;
    private volatile boolean initialised;


    private  NettyContainerConfig nettyContainerConfig;

    private Map<String, Object> attributes;
    private String servletContextName;

    public NettyEmbeddedContext(NettyContainerConfig nettyContainerConfig,String contextPath, ClassLoader classLoader, String serverInfo) {
        this.nettyContainerConfig=nettyContainerConfig;
        this.contextPath = contextPath;
        this.classLoader = classLoader;
        this.serverInfo = new AsciiString(serverInfo);
    }

    public NettyContainerConfig getNettyContainerConfig() {
        return nettyContainerConfig;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void checkNotInitialised() {
        checkState(!isInitialised(), "This method may not be called after the context has been initialised");
    }

    public void addServletMapping(String urlPattern, String name) {
        checkNotInitialised();
        servletMappings.put(urlPattern, checkNotNull(name));
    }

    public void addFilterMapping(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String urlPattern) {
        checkNotInitialised();
        // TODO
    }

    @Override
    public ServletContext getContext(String uripath) {
        return this;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return Utils.getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        throw new IllegalStateException("Method 'getResourcePaths' not yet implemented!");
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return NettyEmbeddedContext.class.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return NettyEmbeddedContext.class.getResourceAsStream(path);
    }

    public String getMatchingUrlPattern(String uri) {
        int indx = uri.indexOf('?');

        String path = indx != -1 ? uri.substring(0, indx) : uri.substring(0);
        String _path = path;
        if (!path.endsWith("/")) _path += "/";

        for (Entry<String, String> entry : servletMappings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Pattern pattern = Pattern.compile(key);
            if (pattern.matcher(_path).matches()) {
                String sanitizedUrlPattern = key.replaceAll("\\*", ".*");
                if (sanitizedUrlPattern.endsWith("/")) {
                    sanitizedUrlPattern = sanitizedUrlPattern.substring(0, sanitizedUrlPattern.length() - 1);
                }
                return sanitizedUrlPattern;
            }
        }
        return path;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        String servletName = servletMappings.get(path);
        if (servletName == null) {
            servletName = servletMappings.get("/");
        }
        Servlet servlet;
        try {
            servlet = null == servletName ? null : servlets.get(servletName).getServlet();
            if (servlet == null) {
                return null;
            }
            final List<Filter> filters = this.filters.values().stream()
                    .map(nettyFilterRegistration -> {
                        try {
                            return nettyFilterRegistration.getFilter();
                        } catch (ServletException e) {
                            return null;
                        }
                    }).collect(toList());
            FilterChain filterChain = new SimpleFilterChain(servlet, filters);
            return new NettyRequestDispatcher(this, filterChain);
        } catch (ServletException e) {
            // TODO log exception
            return null;
        }
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return servlets.get(name).getServlet();
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return Collections.emptyEnumeration();
    }

    @Override
    public Enumeration<String> getServletNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public void log(String msg) {
        logger.info(msg);
    }

    @Override
    public void log(Exception exception, String msg) {
        logger.error(msg, exception);
    }

    @Override
    public void log(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        if ("/".equals(path)) {
            try {
                File file = File.createTempFile("netty-servlet-bridge", "");
                file.mkdirs();
                return file.getAbsolutePath();
            } catch (IOException e) {
                throw new IllegalStateException("Method 'getRealPath' not yet implemented!");
            }
        } else {
            throw new IllegalStateException("Method 'getRealPath' not yet implemented!");
        }
    }

    @Override
    public String getServerInfo() {
        return serverInfo.toString();
    }

    public AsciiString getServerInfoAscii() {
        return serverInfo;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public void setAttribute(String name, Object object) {
        if (this.attributes == null) this.attributes = new HashMap<String, Object>();
        this.attributes.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        if (this.attributes != null) this.attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return this.servletContextName;
    }

    void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName(), servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    private ServletRegistration.Dynamic addServlet(String servletName, String className, Servlet servlet) {
        NettyServletRegistration servletRegistration = new NettyServletRegistration(this, servletName, className,
                servlet);
        servlets.put(servletName, servletRegistration);
        return servletRegistration;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass().getName(), filter);
    }

    private FilterRegistration.Dynamic addFilter(String filterName, String className, Filter filter) {
        NettyFilterRegistration filterRegistration = new NettyFilterRegistration(this, filterName, className, filter);
        filters.put(filterName, filterRegistration);
        return filterRegistration;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filters.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return ImmutableMap.copyOf(filters);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) throws IllegalStateException,
            IllegalArgumentException {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        return null;
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }
}
