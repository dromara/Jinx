
package com.happylifeplat.netty.container;

import com.happylifeplat.netty.context.NettyEmbeddedContext;
import com.happylifeplat.netty.handler.RequestDispatcherHandler;
import com.happylifeplat.netty.handler.ServletContentHandler;
import com.happylifeplat.netty.utils.ChannelThreadLocal;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * netty服务初始化
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
public class NettyEmbeddedServletInitializer extends ChannelInitializer<Channel> {

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
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslContext != null) {
            SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
            sslEngine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(sslEngine));
        }
        // 增加 LineBasedFrameDecoder 和StringDecoder编码器
        pipeline.addLast(new LineBasedFrameDecoder(1024));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 1024 * 64));
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("handler", new ServletContentHandler(servletContext));
        pipeline.addLast(servletExecutor, "filterChain", requestDispatcherHandler);
        ChannelThreadLocal.set(ch);
    }
}
