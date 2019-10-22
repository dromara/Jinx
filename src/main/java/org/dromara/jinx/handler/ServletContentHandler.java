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
package org.dromara.jinx.handler;

import org.dromara.jinx.context.NettyEmbeddedContext;
import org.dromara.jinx.http.HttpContentInputStream;
import org.dromara.jinx.http.NettyHttpServletRequest;
import org.dromara.jinx.http.NettyHttpServletResponse;
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
 * @author xiaoyu
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
