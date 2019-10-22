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
package org.dromara.jinx.http;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;


/**
 * @author xiaoyu
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
