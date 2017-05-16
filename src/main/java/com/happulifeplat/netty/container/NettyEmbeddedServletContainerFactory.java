package com.happulifeplat.netty.container;

import com.happulifeplat.netty.config.NettyContainerConfig;
import com.happulifeplat.netty.context.NettyEmbeddedContext;
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
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * 重写spring-boot默认的 AbstractEmbeddedServletContainerFactory
 * 提供netty容器工厂
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
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
