
package com.happylifeplat.netty.http;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

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
public class HttpPostRequestParameters {

    private final HttpRequest               request;
    private final ByteBuf                   content;

    private final Map<String, List<String>> params  = Maps.newHashMap();
    private byte[]                          body;
    private boolean                         _parsed = false;

    public HttpPostRequestParameters(HttpRequest request, ByteBuf content) {
        this.request = request;
        this.content = content;
    }

    byte[] getHttpRequestBodyAsBytes() {
        if (body != null) {
            return body;
        }
        if (content == null) {
            return null;
        }

        content.readerIndex(0);
        int len = content.readableBytes();
        body = new byte[len];
        content.readBytes(body);

        return body;
    }

    Map<String, List<String>> getHttpRequestParameters() {
        if (_parsed) {
            return params;
        }

        byte[] body = getHttpRequestBodyAsBytes();
        if (body == null) {
            _parsed = true;
            return params;
        }

        QueryStringDecoder decoder = new QueryStringDecoder("?" + new String(body));
        params.putAll(decoder.parameters());
        _parsed = true;
        return params;
    }
}
