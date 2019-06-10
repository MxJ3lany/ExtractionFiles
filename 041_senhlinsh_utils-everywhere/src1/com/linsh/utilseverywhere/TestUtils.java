package com.linsh.utilseverywhere;

/**
 * <pre>
 *    author : Senh Linsh
 *    github : https://github.com/SenhLinsh
 *    date   : 2017/11/13
 *    desc   : 工具类: 测试相关
 *
 *          该工具类不再使用
 * </pre>
 */
@Deprecated
public class TestUtils {

    private TestUtils() {
    }

    /**
     * 单行打印测试日志
     *
     * @param content 消息内容
     */
    public static void print(Object content) {
        System.err.print(content.toString());
    }

    /**
     * 单行打印测试日志
     *
     * @param contents 消息内容
     */
    public static void print(Object... contents) {
        if (contents == null || contents.length == 0) return;
        for (Object object : contents) {
            System.err.print(object + "    ");
        }
    }

    /**
     * 隔行打印测试日志
     *
     * @param content 消息内容
     */
    public static void printLn(Object content) {
        System.err.println(content);
    }

    /**
     * 隔行打印测试日志
     *
     * @param contents 消息内容
     */
    public static void printLn(Object... contents) {
        if (contents == null || contents.length == 0) {
            System.err.println();
            return;
        }
        for (Object object : contents) {
            System.err.println(object);
        }
    }

    /**
     * 获取运行可执行任务的运行时长
     *
     * @param runnable 可执行任务
     * @return 时长
     */
    public static long getRunTime(Runnable runnable) {
        return getRunTime(runnable, 1);
    }

    /**
     * 获取运行可执行任务的运行时长
     *
     * @param runnable 可执行任务
     * @param times    循环次数
     * @return 时长
     */
    public static long getRunTime(Runnable runnable, int times) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            runnable.run();
        }
        return System.currentTimeMillis() - startTime;
    }
}
