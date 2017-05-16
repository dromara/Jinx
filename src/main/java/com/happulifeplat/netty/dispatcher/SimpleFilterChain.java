/*
 * Copyright 2015-2020 msun.com All right reserved.
 */
package com.happulifeplat.netty.dispatcher;

import io.netty.handler.codec.http.HttpResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *  FilterChain 先执行所有的filter 最后 执行SpringMvc的调用
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
public class SimpleFilterChain implements FilterChain {

    private final Iterator<Filter> filterIterator;
    private final Servlet servlet;

    public SimpleFilterChain(Servlet servlet, Iterable<Filter> filters) throws ServletException {
        this.filterIterator = checkNotNull(filters).iterator();
        this.servlet = checkNotNull(servlet);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (filterIterator.hasNext()) {
            Filter filter = filterIterator.next();
            filter.doFilter(request, response, this);
        } else {
            servlet.service(request, response);
        }
    }
}
