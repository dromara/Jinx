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
package org.dromara.jinx.registration;


import org.dromara.jinx.context.NettyEmbeddedContext;

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
 * @author xiaoyu
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
