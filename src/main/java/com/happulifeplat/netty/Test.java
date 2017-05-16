package com.happulifeplat.netty;

/**
 * <p>Description: .</p>
 * <p>Company: 深圳市旺生活互联网科技有限公司</p>
 * <p>Copyright: 2015-2017 happylifeplat.com All Rights Reserved</p>
 *
 * @author yu.xiao@happylifeplat.com
 * @version 1.0
 * @date 2017/5/12 16:46
 * @since JDK 1.8
 */
public class Test {

    public static void main(String[] args) {
        String uri="/app-background/advertisement/open-app";
        String context="/app-background";

        final String substring1 = uri.substring(context.length(), uri.length());
        final String substring = uri.substring(uri.indexOf(context));
        System.out.println(substring);
    }
}
