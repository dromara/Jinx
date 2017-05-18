
package com.happylifeplat.netty.handler;

import com.happylifeplat.netty.context.NettyEmbeddedContext;
import com.happylifeplat.netty.dispatcher.NettyRequestDispatcher;
import com.happylifeplat.netty.http.NettyHttpServletRequest;
import com.happylifeplat.netty.utils.ChannelThreadLocal;
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


/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * netty servlet处理 在inbound中流转
 * 经过filter过滤，最后进行springMvc调用
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
@ChannelHandler.Sharable
public class RequestDispatcherHandler extends SimpleChannelInboundHandler<NettyHttpServletRequest> {

    private final Log logger = LogFactory.getLog(getClass());
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
        }
        finally {
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
