/**
 * @author yu.xiao
 * @version 1.0
 * @date 2017/5/16 9:24
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
