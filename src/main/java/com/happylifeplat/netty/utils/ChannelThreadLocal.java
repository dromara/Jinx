
package com.happylifeplat.netty.utils;

import io.netty.channel.Channel;

public class ChannelThreadLocal {

    public static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<Channel>();

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
