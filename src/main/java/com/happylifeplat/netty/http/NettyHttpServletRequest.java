
package com.happylifeplat.netty.http;

import com.happylifeplat.netty.config.NettyContainerConfig;
import com.happylifeplat.netty.context.NettyAsyncContext;
import com.happylifeplat.netty.context.NettyEmbeddedContext;
import com.happylifeplat.netty.dispatcher.NettyRequestDispatcher;
import com.happylifeplat.netty.session.HttpSessionThreadLocal;
import com.happylifeplat.netty.session.NettyHttpSession;
import com.happylifeplat.netty.utils.ChannelThreadLocal;
import com.happylifeplat.netty.utils.URIParser;
import com.happylifeplat.netty.utils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.WWW_AUTHENTICATE;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * {@link HttpServletRequest} wrapper for Netty's {@link HttpRequest}.
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
public class NettyHttpServletRequest implements HttpServletRequest {

    private static final Locale         DEFAULT_LOCALE  = Locale.getDefault();
    public static final String          DISPATCHER_TYPE = NettyRequestDispatcher.class.getName() + ".DISPATCHER_TYPE";

    private final ChannelHandlerContext ctx;
    private final NettyEmbeddedContext servletContext;
    private final HttpRequest           request;
    private final ServletInputStream    inputStream;
    private final Map<String, Object>   attributes;
    private final QueryStringDecoder    queryStringDecoder;
    private Principal                   userPrincipal;

    private boolean                     asyncSupported  = true;
    private NettyAsyncContext asyncContext;
    private HttpServletResponse         servletResponse;
    private String                      characterEncoding;
    private BufferedReader              reader;
    private Map<String, List<String>>   params;
    private URIParser uriParser;

    public NettyHttpServletRequest(ChannelHandlerContext ctx, NettyEmbeddedContext servletContext, HttpRequest request,
                            HttpServletResponse servletResponse, HttpContentInputStream inputStream) {
        this.ctx = ctx;
        this.servletContext = servletContext;
        this.request = request;
        this.servletResponse = servletResponse;
        this.inputStream = inputStream;
        this.attributes = new ConcurrentHashMap<>();

        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        this.queryStringDecoder = new QueryStringDecoder(request.uri());
        this.characterEncoding = Utils.getCharsetFromContentType(getContentType());
        this.uriParser = new URIParser(servletContext);
        this.uriParser.parse(request.uri());

        if (HttpMethod.POST.equals(request.getMethod())) {
            if (request instanceof FullHttpRequest) {
                HttpPostRequestParameters httpPostRequestParameters = new HttpPostRequestParameters(
                                                                                                    request,
                                                                                                    ((FullHttpRequest) request).content());
                params = httpPostRequestParameters.getHttpRequestParameters();
            } else {
                params = Collections.emptyMap();
            }
        } else {
            params = this.queryStringDecoder.parameters();
        }
    }

    HttpRequest getNettyRequest() {
        return request;
    }

    @Override
    public String getAuthType() {
        return getHeader(WWW_AUTHENTICATE);
    }

    @Override
    public Cookie[] getCookies() {
        String cookieString = this.request.headers().get(COOKIE);
        if (cookieString != null) {
            Set<io.netty.handler.codec.http.Cookie> cookies = CookieDecoder.decode(cookieString);
            if (!cookies.isEmpty()) {
                Cookie[] cookiesArray = new Cookie[cookies.size()];
                int indx = 0;
                for (io.netty.handler.codec.http.Cookie c : cookies) {
                    Cookie cookie = new Cookie(c.getName(), c.getValue());
                    cookie.setComment(c.getComment());
                    cookie.setDomain(c.getDomain());
                    cookie.setMaxAge((int) c.getMaxAge());
                    cookie.setPath(c.getPath());
                    cookie.setSecure(c.isSecure());
                    cookie.setVersion(c.getVersion());
                    cookiesArray[indx] = cookie;
                    indx++;
                }
                return cookiesArray;

            }
        }
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        String longVal = getHeader(name);
        if (longVal == null) return -1;
        return Long.parseLong(longVal);
    }

    @Override
    public String getHeader(String name) {
        return HttpHeaders.getHeader(this.request, name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Utils.enumeration(this.request.headers().getAll(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Utils.enumeration(this.request.headers().names());
    }

    @Override
    public int getIntHeader(String name) {
        return HttpHeaders.getIntHeader(this.request, name, -1);
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getPathInfo() {
        return this.uriParser.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return null;
    }


    @Override
    public String getContextPath() {
        String requestURI = getRequestURI();
        // FIXME implement properly
        return "/".equals(requestURI) ? "" : requestURI;
    }

    @Override
    public String getQueryString() {
        return this.uriParser.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return getHeader(AUTHORIZATION);
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }


    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public String getRequestedSessionId() {
        NettyHttpSession session = HttpSessionThreadLocal.get();
        return session != null ? session.getId() : null;
    }

    @Override
    public String getRequestURI() {
        // return request.uri();
        return this.uriParser.getRequestUri();
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = this.getScheme();
        int port = this.getServerPort();
        String urlPath = this.getRequestURI();

        // String servletPath = req.getServletPath ();
        // String pathInfo = req.getPathInfo ();

        url.append(scheme); // http, https
        url.append("://");
        url.append(this.getServerName());
        if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
            url.append(':');
            url.append(this.getServerPort());
        }
        // if (servletPath != null)
        // url.append (servletPath);
        // if (pathInfo != null)
        // url.append (pathInfo);
        url.append(urlPath);
        return url;
    }

    @Override
    public String getServletPath() {
        String servletPath = this.uriParser.getServletPath();
        if (servletPath.equals("/")) return "";
        return servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        HttpSession session = HttpSessionThreadLocal.get();
        if (session == null && create) {
            session = HttpSessionThreadLocal.getOrCreate();
        }
        return session;
    }

    @Override
    public HttpSession getSession() {
        HttpSession s = HttpSessionThreadLocal.getOrCreate();
        return s;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new IllegalStateException("Method 'isRequestedSessionIdValid' not yet implemented!");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return true;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new IllegalStateException("Method 'isRequestedSessionIdFromURL' not yet implemented!");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new IllegalStateException("Method 'isRequestedSessionIdFromUrl' not yet implemented!");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        synchronized (attributes) {
            return attributes.get(name);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        synchronized (attributes) {
            return Collections.enumeration(attributes.keySet());
        }
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        return (int) HttpHeaders.getContentLength(this.request, -1);
    }

    @Override
    public long getContentLengthLong() {
        return (long) HttpHeaders.getContentLength(this.request, -1);
    }

    @Override
    public String getContentType() {
        return HttpHeaders.getHeader(this.request, Names.CONTENT_TYPE);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return values != null ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Utils.enumerationFromKeys(this.params);
    }

    @Override
    public String[] getParameterValues(String name) {
        List<String> values = this.params.get(name);
        if (values == null || values.isEmpty()) return null;
        return values.toArray(new String[values.size()]);
    }

    @Override
    public Map getParameterMap() {
        return this.params;
    }

    @Override
    public String getProtocol() {
        return request.protocolVersion().protocolName();
    }

    @Override
    public String getScheme() {
	try {
	    return this.isSecure() ? "https" : "http";
        } catch (Exception e) {
            return "http";
        }
    }

    @Override
    public String getServerName() {
        final Channel channel = ChannelThreadLocal.get();
        if(Objects.nonNull(channel)){
            final Optional<SocketAddress> socketAddress = Optional.ofNullable(channel.localAddress());
            if(socketAddress.isPresent()){
                InetSocketAddress addr= (InetSocketAddress)socketAddress.get();
                return addr.getHostName();
            }
        }
        return "";
    }

    @Override
    public int getServerPort() {
        NettyContainerConfig nettyContainerConfig = servletContext.getNettyContainerConfig();
        return nettyContainerConfig.getPORT();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return this.reader;
    }

    @Override
    public String getRemoteAddr() {
        return null;
        // InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().remoteAddress();
        // return addr.getAddress().getHostAddress();
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().remoteAddress();
        return addr.getHostName();
    }

    @Override
    public void setAttribute(String name, Object o) {
        synchronized (attributes) {
            attributes.put(name, o);
        }
    }

    @Override
    public void removeAttribute(String name) {
        synchronized (attributes) {
            attributes.remove(name);
        }
    }

    @Override
    public Locale getLocale() {
        String locale = HttpHeaders.getHeader(this.request, Names.ACCEPT_LANGUAGE, DEFAULT_LOCALE.toString());
        return new Locale(locale);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        Collection<Locale> locales = Utils.parseAcceptLanguageHeader(HttpHeaders.getHeader(this.request,
                                                                                           Names.ACCEPT_LANGUAGE));

        if (locales == null || locales.isEmpty()) {
            locales = new ArrayList<Locale>();
            locales.add(Locale.getDefault());
        }
        return Utils.enumeration(locales);
    }

    @Override
    public boolean isSecure() {
        NettyContainerConfig nettyContainerConfig = servletContext.getNettyContainerConfig();
        return nettyContainerConfig.isSsl();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new IllegalStateException("Method 'getRequestDispatcher' not yet implemented!");
    }

    @Override
    public String getRealPath(String path) {
        throw new IllegalStateException("Method 'getRealPath' not yet implemented!");
    }

    @Override
    public int getRemotePort() {
        InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().remoteAddress();
        return addr.getPort();
    }

    @Override
    public String getLocalName() {
        return getServerName();
    }

    @Override
    public String getLocalAddr() {
        InetSocketAddress addr = (InetSocketAddress) ChannelThreadLocal.get().localAddress();
        return addr.getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return getServerPort();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return ((NettyAsyncContext) getAsyncContext()).startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return null != asyncContext && asyncContext.isAsyncStarted();
    }

    void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    @Override
    public AsyncContext getAsyncContext() {
        if (null == asyncContext) {
            asyncContext = new NettyAsyncContext(this, ctx);
        }
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return attributes.containsKey(DISPATCHER_TYPE) ? (DispatcherType) attributes.get(DISPATCHER_TYPE) : DispatcherType.REQUEST;
    }

    public ServletResponse getServletResponse() {
        return servletResponse;
    }
}
