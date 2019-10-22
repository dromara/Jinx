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

import org.dromara.jinx.config.NettyContainerConfig;
import org.dromara.jinx.context.NettyEmbeddedContext;
import io.netty.bootstrap.Bootstrap;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.ClassUtils;

import javax.servlet.ServletException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author xiaoyu
 */
public class NettyEmbeddedServletContainerFactory extends AbstractEmbeddedServletContainerFactory implements ResourceLoaderAware {

    private static final String SERVER_INFO = "netty/servlet";
    private ResourceLoader resourceLoader;
    private NettyContainerConfig nettyContainerConfig;

    public NettyEmbeddedServletContainerFactory(NettyContainerConfig nettyContainerConfig) {
        super();
        this.nettyContainerConfig = nettyContainerConfig;
    }


    @Override
    public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
        ClassLoader parentClassLoader = resourceLoader != null ? resourceLoader.getClassLoader() : ClassUtils.getDefaultClassLoader();
        Package nettyPackage = Bootstrap.class.getPackage();
        String title = nettyPackage.getImplementationTitle();
        String version = nettyPackage.getImplementationVersion();
        logger.info("Running with " + title + " " + version);
        NettyEmbeddedContext context = new NettyEmbeddedContext(nettyContainerConfig,getContextPath(),
                new URLClassLoader(new URL[]{}, parentClassLoader),
                SERVER_INFO);
        if (isRegisterDefaultServlet()) {
            logger.warn("This container does not support a default servlet");
        }
        for (ServletContextInitializer initializer : initializers) {
            try {
                initializer.onStartup(context);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
        logger.info("nettyContainerConfig :"+nettyContainerConfig.toString());
        return new NettyEmbeddedServletContainer(nettyContainerConfig, context);
    }

    /**
     * Set the ResourceLoader that this object runs in.
     * <p>This might be a ResourcePatternResolver, which can be checked
     * through {@code instanceof ResourcePatternResolver}. See also the
     * {@code ResourcePatternUtils.getResourcePatternResolver} method.
     * <p>Invoked after population of normal bean properties but before an init callback
     * like InitializingBean's {@code afterPropertiesSet} or a custom init-method.
     * Invoked before ApplicationContextAware's {@code setApplicationContext}.
     *
     * @param resourceLoader ResourceLoader object to be used by this object
     * @see ResourcePatternResolver
     * @see ResourcePatternUtils#getResourcePatternResolver
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
    }
}
