/*
 * Copyright 2015-2020 msun.com All right reserved.
 */
package com.happylifeplat.netty.dispatcher;

import com.happylifeplat.netty.http.NettyHttpServletRequest;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


public class NettyRequestDispatcher implements RequestDispatcher {

    @SuppressWarnings("unused")
    private final ServletContext context;
    private final FilterChain filterChain;

    public NettyRequestDispatcher(ServletContext context, FilterChain filterChain) {
        this.context = context;
        this.filterChain = filterChain;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.FORWARD);
        // TODO implement
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.INCLUDE);
        // TODO implement
    }

    public void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.ASYNC);
        filterChain.doFilter(request, response);
    }
}
