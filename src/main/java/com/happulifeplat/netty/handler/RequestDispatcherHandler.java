
package com.happulifeplat.netty.handler;

import com.happulifeplat.netty.context.NettyEmbeddedContext;
import com.happulifeplat.netty.dispatcher.NettyRequestDispatcher;
import com.happulifeplat.netty.http.NettyHttpServletRequest;
import com.happulifeplat.netty.utils.ChannelThreadLocal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


@ChannelHandler.Sharable
public class RequestDispatcherHandler extends SimpleChannelInboundHandler<NettyHttpServletRequest> {

    private final Log                  logger = LogFactory.getLog(getClass());
    private final NettyEmbeddedContext context;

    public RequestDispatcherHandler(NettyEmbeddedContext context) {
        this.context = checkNotNull(context);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpServletRequest request) throws Exception {
        HttpServletResponse servletResponse = (HttpServletResponse) request.getServletResponse();
        try {
            NettyRequestDispatcher dispatcher = (NettyRequestDispatcher)
                    context.getRequestDispatcher(request.getRequestURI());
            if (dispatcher == null) {
                sendError(ctx, HttpResponseStatus.BAD_GATEWAY);
                return;
            }
            dispatcher.dispatch(request, servletResponse);
            ChannelFuture writeFuture = ctx.writeAndFlush(servletResponse);
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ChannelThreadLocal.unset();
            if (!request.isAsyncStarted()) {
                try {
                    servletResponse.getOutputStream().close();
                } catch (Exception e) {
                }
                try {
                    servletResponse.getWriter().close();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception caught during request", cause);
        ctx.close();
    }

    /**
     * 发送错误信息
     *
     * @param ctx
     * @param status
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                HTTP_1_1,
                status,
                content
        );
        fullHttpResponse.headers().add(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
