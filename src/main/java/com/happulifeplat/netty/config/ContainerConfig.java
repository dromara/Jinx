
package com.happulifeplat.netty.config;

import org.springframework.web.servlet.DispatcherServlet;


/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *  容器配置
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/11 18:02
 * @since JDK 1.8
 */
public class ContainerConfig {
    /**
     * 启动服务端口
     */
    protected int port;
    /**
     * 初始化出来的spring mvc Servlet
     */
    protected DispatcherServlet dispatcherServlet;
    /**
     * 最大线程数
     */
    protected int maxThreads;
    /**
     * ssl安全文件地址
     * 这里只支持jks文件类型
     */
    protected String caPath;
    /**
     * ssl安全文件密码
     */
    protected String caPassWord;
    /**
     * 是否使用https加密方式
     */
    protected boolean ssl;

    public int getPORT() {
        return port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public String getCaPath() {
        return caPath;
    }

    public String getCaPassWord() {
        return caPassWord;
    }

    public boolean isSsl() {
        return ssl;
    }

    /**
     * 设置的servlet 属性
     *
     * @return
     */
    public DispatcherServlet getDispatcherServlet() {
        return dispatcherServlet;
    }

    public void setDispatcherServlet(DispatcherServlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
    }
}
