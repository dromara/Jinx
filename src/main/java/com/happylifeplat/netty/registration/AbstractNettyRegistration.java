
package com.happylifeplat.netty.registration;


import com.happylifeplat.netty.context.NettyEmbeddedContext;

import javax.servlet.FilterConfig;
import javax.servlet.Registration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *  动态获取servlet filter 抽象父类
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/12 16:46
 * @since JDK 1.8
 */
public abstract class  AbstractNettyRegistration implements Registration, Registration.Dynamic, ServletConfig, FilterConfig {

    private final String               name;
    private final String               className;
    private final NettyEmbeddedContext context;
    protected boolean                  asyncSupported;

    protected AbstractNettyRegistration(String name, String className, NettyEmbeddedContext context) {
        this.name = checkNotNull(name);
        this.className = checkNotNull(className);
        this.context = checkNotNull(context);
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        asyncSupported = isAsyncSupported;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        checkArgument(name != null, "name may not be null");
        checkArgument(value != null, "value may not be null");
        return false;
    }

    @Override
    public String getFilterName() {
        return name;
    }

    @Override
    public String getServletName() {
        return name;
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    protected NettyEmbeddedContext getNettyContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return Collections.emptyMap();
    }
}
