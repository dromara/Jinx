
package com.happylifeplat.netty.registration;

import com.happylifeplat.netty.context.NettyEmbeddedContext;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.EnumSet;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * 获取所有的filter对象
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
public class NettyFilterRegistration extends AbstractNettyRegistration implements FilterRegistration.Dynamic {

    private volatile boolean initialised;
    private Filter filter;

    public NettyFilterRegistration(NettyEmbeddedContext context, String filterName, String className, Filter filter) {
        super(filterName, className, context);
        this.filter = filter;
    }

    public Filter getFilter() throws ServletException {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {
                    if (null == filter) {
                        try {
                            filter = (Filter) Class.forName(getClassName()).newInstance();
                        } catch (Exception e) {
                            throw new ServletException(e);
                        }
                    }
                    filter.init(this);
                    initialised = true;
                }
            }
        }
        return filter;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
                                          String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
                                         String... urlPatterns) {
        NettyEmbeddedContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            context.addFilterMapping(dispatcherTypes, isMatchAfter, urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return null;
    }
}
