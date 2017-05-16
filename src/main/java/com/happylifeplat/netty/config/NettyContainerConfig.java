
package com.happylifeplat.netty.config;


/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * Netty容器配置
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
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

