
package com.happylifeplat.netty.utils;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/12 16:46
 * @since JDK 1.8
 */
public class Utils {

    private Utils() {
    }

    public static final <T> Enumeration<T> emptyEnumeration() {
        return Collections.enumeration(Collections.<T> emptySet());
    }

    public static final <T> Enumeration<T> enumeration(Collection<T> collection) {
        if (collection == null) return emptyEnumeration();

        return Collections.enumeration(collection);
    }

    public static final <T> Enumeration<T> enumerationFromKeys(Map<T, ?> map) {
        if (map == null) return emptyEnumeration();

        return Collections.enumeration(map.keySet());
    }

    public static final <T> Enumeration<T> enumerationFromValues(Map<?, T> map) {
        if (map == null) return emptyEnumeration();

        return Collections.enumeration(map.values());
    }

    public static final <T> T newInstance(Class<T> clazz) {

        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Error instantiating class: " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error instantiating class: " + clazz, e);
        }

    }

    /**
     * Parse the character encoding from the specified content type header. If the content type is null, or there is no
     * explicit character encoding, <code>null</code> is returned.
     *
     * @param contentType a content type header
     */
    public static final String getCharsetFromContentType(String contentType) {

        if (contentType == null) {
            return (null);
        }
        int start = contentType.indexOf("charset=");
        if (start < 0) {
            return (null);
        }
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\"")) && (encoding.endsWith("\""))) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }
        return (encoding.trim());

    }

    public static final Collection<Cookie> getCookies(String name, HttpRequest request) {
        String cookieString = request.headers().get(COOKIE);
        if (cookieString != null) {
            List<Cookie> foundCookie = new ArrayList<Cookie>();
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) foundCookie.add(cookie);
            }

            return foundCookie;
        }
        return null;
    }

    public static final Collection<Cookie> getCookies(String name, HttpResponse response) {
        String cookieString = response.headers().get(COOKIE);
        if (cookieString != null) {
            List<Cookie> foundCookie = new ArrayList<Cookie>();
            Set<Cookie> cookies = CookieDecoder.decode(cookieString);
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) foundCookie.add(cookie);
            }

            return foundCookie;
        }
        return null;
    }

    public static final String getMimeType(String fileUrl) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileUrl);
        return type;
    }

    public static final String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") || uri.contains("." + File.separator) || uri.startsWith(".")
            || uri.endsWith(".")) {
            return null;
        }

        return uri;
    }

    public static final Collection<Locale> parseAcceptLanguageHeader(String acceptLanguageHeader) {

        if (acceptLanguageHeader == null) return null;

        List<Locale> locales = new ArrayList<Locale>();

        for (String str : acceptLanguageHeader.split(",")) {
            String[] arr = str.trim().replace("-", "_").split(";");

            // Parse the locale
            Locale locale = null;
            String[] l = arr[0].split("_");
            switch (l.length) {
                case 2:
                    locale = new Locale(l[0], l[1]);
                    break;
                case 3:
                    locale = new Locale(l[0], l[1], l[2]);
                    break;
                default:
                    locale = new Locale(l[0]);
                    break;
            }

            // Parse the q-value
            /*
             * Double q = 1.0D; for (String s : arr) { s = s.trim(); if (s.startsWith("q=")) { q =
             * Double.parseDouble(s.substring(2).trim()); break; } }
             */

            locales.add(locale);
        }
        return locales;
    }
}
