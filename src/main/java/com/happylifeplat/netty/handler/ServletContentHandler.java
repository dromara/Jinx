
package com.happylifeplat.netty.handler;

import com.happylifeplat.netty.context.NettyEmbeddedContext;
import com.happylifeplat.netty.http.HttpContentInputStream;
import com.happylifeplat.netty.http.NettyHttpServletRequest;
import com.happylifeplat.netty.http.NettyHttpServletResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * netty servlet处理 在inbound中流转
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
@ChannelHandler.Sharable
public class ServletContentHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final NettyEmbeddedContext servletContext;
    private HttpContentInputStream inputStream;

    public ServletContentHandler(NettyEmbeddedContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        inputStream = new HttpContentInputStream(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request != null) {
            //X-Real-IP
            Optional<String> option = Optional.ofNullable(request.headers().get("X-Forwarded-For"));
            String real = option.orElseGet(() -> request.headers().get("X-Real-IP"));
            //X-Forwarded-For
            if (StringUtils.isBlank(real)) {
                InetSocketAddress socketAddr = (InetSocketAddress) ctx.channel().remoteAddress();
                real = socketAddr.getAddress().getHostAddress();
            }
            /**
             * 如果发送生错误
             */
            if (!request.decoderResult().isSuccess()) {
                sendError(ctx, HttpResponseStatus.BAD_GATEWAY);
                return;
            }

            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, false);
            NettyHttpServletResponse servletResponse
                    = new NettyHttpServletResponse(ctx, servletContext, response);
            NettyHttpServletRequest servletRequest
                    = new NettyHttpServletRequest(ctx, servletContext, request,
                    servletResponse, inputStream);
            if (HttpHeaders.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE),
                        ctx.voidPromise());
            }
            ctx.fireChannelRead(servletRequest);
        }
        if (request != null) {
            inputStream.addContent(request);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        inputStream.close();
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
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
