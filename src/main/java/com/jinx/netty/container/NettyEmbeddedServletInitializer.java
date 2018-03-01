/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jinx.netty.container;

import com.jinx.netty.context.NettyEmbeddedContext;
import com.jinx.netty.handler.RequestDispatcherHandler;
import com.jinx.netty.handler.ServletContentHandler;
import com.jinx.netty.utils.ChannelThreadLocal;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author xiaoyu
 */
public class NettyEmbeddedServletInitializer extends ChannelInitializer<SocketChannel> {

    private final EventExecutorGroup servletExecutor;
    private final RequestDispatcherHandler requestDispatcherHandler;
    private final NettyEmbeddedContext servletContext;
    private SslContext sslContext;

    public NettyEmbeddedServletInitializer(EventExecutorGroup servletExecutor, NettyEmbeddedContext servletContext, SslContext sslContext) {
        this.servletContext = servletContext;
        this.servletExecutor = checkNotNull(servletExecutor);
        this.sslContext=sslContext;
        requestDispatcherHandler = new RequestDispatcherHandler(servletContext);

    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
            sslEngine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(sslEngine));
        }
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 64));
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("handler", new ServletContentHandler(servletContext));
        pipeline.addLast(servletExecutor, "filterChain", requestDispatcherHandler);
        ChannelThreadLocal.set(ch);
    }
}
