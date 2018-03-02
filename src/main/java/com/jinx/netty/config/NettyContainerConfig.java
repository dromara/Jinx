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
package com.jinx.netty.config;


/**
 * @author xiaoyu
 */
public class NettyContainerConfig extends ContainerConfig {

    /**
     * 服务启动端口
     */
    protected final String PORT_CONFIG = "http.port";
    /**
     * 最大线程数
     */
    protected final String MAX_THREADS = "max.thread";
    /**
     * https CA证书加密安全文件 *.jks
     */
    protected final String CA_PATH = "http.ca.path";
    /**
     * https CA证书 加密安全文件密码
     */
    protected final String CA_PASSWORD = "http.ca.password";
    /**
     * 是否启用https访问
     */
    protected final String SSL = "SSL";

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 设置值
     *
     * @param builder
     * @return
     */
    private NettyContainerConfig setValue(Builder builder) {
        this.port = builder.port;
        this.maxThreads = builder.maxThreads;
        this.caPassWord = builder.caPassword;
        this.caPath = builder.caPath;
        this.ssl = builder.ssl;
        return this;
    }

    @Override
    public String toString() {
        return "NettyContainerConfig{" +
                "PORT_CONFIG='" + PORT_CONFIG + '\'' +
                ", MAX_THREADS='" + MAX_THREADS + '\'' +
                ", CA_PATH='" + CA_PATH + '\'' +
                ", CA_PASSWORD='" + CA_PASSWORD + '\'' +
                ", SSL='" + SSL + '\'' +
                '}';
    }

    /**
     * 构建器
     */
    public static class Builder {
        private int port = 8888;
        private int maxThreads = Runtime.getRuntime().availableProcessors();
        private String caPath = null;
        private String caPassword = "";
        private boolean ssl = false;

        /**
         * CA证书地址
         *
         * @param caPath
         */
        public Builder setCaPath(String caPath) {
            this.caPath = caPath;
            return this;
        }

        /**
         * CA证书密码
         *
         * @param caPassword
         */
        public Builder setCaPassword(String caPassword) {
            this.caPassword = caPassword;
            return this;
        }

        /**
         * 是否启用https服务
         *
         * @param ssl
         */
        public Builder setSsl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        /**
         * 启动的服务端口号:默认 8888
         *
         * @param port
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * 最大处理线程数：默认100
         *
         * @param maxThreads
         */

        public Builder setMaxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public NettyContainerConfig build() {
            return new NettyContainerConfig().setValue(this);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "http.prot=" + port +
                    ", max.thread=" + maxThreads +
                    ", http.ca.path='" + caPath + '\'' +
                    ", http.ca.password='" + caPassword + '\'' +
                    ", ssl=" + ssl +
                    '}';
        }
    }
}

