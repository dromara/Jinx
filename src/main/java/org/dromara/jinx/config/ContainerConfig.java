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
package org.dromara.jinx.config;

import org.springframework.web.servlet.DispatcherServlet;


/**
 * @author xiaoyu
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
