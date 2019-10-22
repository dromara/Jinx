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
package org.dromara.jinx.container;

import com.google.common.base.StandardSystemProperty;
import org.dromara.jinx.config.NettyContainerConfig;
import org.dromara.jinx.context.NettyEmbeddedContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author xiaoyu
 */
public class NettyEmbeddedServletContainer implements EmbeddedServletContainer {

    private final Log logger = LogFactory.getLog(getClass());
    private final NettyEmbeddedContext context;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup servletExecutor;

    private NettyContainerConfig nettyContainerConfig;

    public NettyEmbeddedServletContainer(NettyContainerConfig nettyContainerConfig, NettyEmbeddedContext context) {
        this.nettyContainerConfig = nettyContainerConfig;
        this.context = context;
    }

    @Override
    public void start() throws EmbeddedServletContainerException {
        SslContext sslcontext = null;
        if (nettyContainerConfig.isSsl()) {//如果启用https访问方式
            KeyManagerFactory kmf;
            InputStream in = null;

            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                in = new FileInputStream(nettyContainerConfig.getCaPath());
                ks.load(in, nettyContainerConfig.getCaPassWord().toCharArray());
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, nettyContainerConfig.getCaPassWord().toCharArray());
                sslcontext = SslContextBuilder.forServer(kmf).build();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {

                    }
                }
            }
        }
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        groups(serverBootstrap);
        servletExecutor = new DefaultEventExecutorGroup(nettyContainerConfig.getMaxThreads() * 2);
        serverBootstrap.childHandler(new NettyEmbeddedServletInitializer(servletExecutor, context,sslcontext));

        // Don't yet need the complexity of lifecycle state, listeners etc, so tell the context it's initialised here
        context.setInitialised(true);

        ChannelFuture future = serverBootstrap.bind(nettyContainerConfig.getPORT()).awaitUninterruptibly();
        // noinspection ThrowableResultOfMethodCallIgnored
        Throwable cause = future.cause();
        if (null != cause) {
            throw new EmbeddedServletContainerException("Could not start Netty server", cause);
        }
        logger.info(context.getServerInfo() + " started on port: " + getPort());
    }

    private void groups(ServerBootstrap b) {
        if (StandardSystemProperty.OS_NAME.value().equals("Linux")) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(nettyContainerConfig.getMaxThreads() * 2);
            b.channel(EpollServerSocketChannel.class).group(bossGroup, workerGroup).option(EpollChannelOption.TCP_CORK,
                    true);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(nettyContainerConfig.getMaxThreads() * 2);
            b.channel(NioServerSocketChannel.class).group(bossGroup, workerGroup);
        }
        b.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_BACKLOG,
                1024);
        logger.info("Bootstrap configuration: " + b.toString());
    }

    @Override
    public void stop() throws EmbeddedServletContainerException {
        try {
            if (null != bossGroup) {
                bossGroup.shutdownGracefully().await();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().await();
            }
            if (null != servletExecutor) {
                servletExecutor.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            throw new EmbeddedServletContainerException("Container stop interrupted", e);
        }
    }

    @Override
    public int getPort() {
        return nettyContainerConfig.getPORT();
    }
}
