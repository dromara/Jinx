
package com.happylifeplat.netty.utils;

import io.netty.channel.Channel;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 * 用threadLocal 存储channel信息
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/12 16:46
 * @since JDK 1.8
 */
public class ChannelThreadLocal {

    public static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

    public static void set(Channel channel) {
        channelThreadLocal.set(channel);
    }

    public static void unset() {
        channelThreadLocal.remove();
    }

    public static Channel get() {
        return channelThreadLocal.get();
    }
}
