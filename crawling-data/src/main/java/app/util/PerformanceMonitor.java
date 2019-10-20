package app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 性能统计
 *
 * @author faith.huan 2018-12-06 01:53:18
 */
public class PerformanceMonitor {
    /**
     * 使用自己的logger,用于预警识别
     */
    static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);

    private static ThreadLocal<MethodPerformance> performanceRecord = new ThreadLocal<>();

    public static void begin(String method) {
        begin(method, true);
    }

    static boolean useStdout = false;

    public static void useStdout() {
        useStdout = true;
    }

    /**
     * 开始统计点
     *
     * @param method 字符串标识
     * @param isInfo 标识是否输出开始统计日志
     * @author faith.huan@2018年2月2日 上午10:15:05
     */
    public static void begin(String method, boolean isInfo) {
        if (isInfo) {
            if (useStdout) {
                System.out.println("PerformanceMonitor->begin monitor " + method);
            } else {
                logger.info("PerformanceMonitor->begin monitor {}", method);
            }
        }
        MethodPerformance mp = new MethodPerformance(method);
        performanceRecord.set(mp);
    }

    public static void end() {
        end(true);
    }

    public static void end(boolean isLog) {
        end(0, isLog);
    }


    /**
     * 时间小于min的不输出耗时日志
     *
     * @param min 打印下限值
     * @author faith.huan@2018年2月2日 上午10:17:43
     */
    public static void end(int min) {
        end(min, true);
    }

    public static void end(int min, boolean isInfo) {
        MethodPerformance mp = performanceRecord.get();
        performanceRecord.remove();
        if (isInfo) {
            if (useStdout) {
                System.out.println("PerformanceMonitor->end monitor " + mp.getServiceMethod());
            } else {
                logger.info("PerformanceMonitor->end monitor {}", mp.getServiceMethod());
            }
        }
        mp.printPerformance(min);
    }
}

class MethodPerformance {
    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private Date begin;
    private String serviceMethod;

    MethodPerformance(String serviceMethod) {
        this.begin = new Date();
        this.serviceMethod = serviceMethod;
    }

    void printPerformance() {
        printPerformance(0);
    }

    void printPerformance(int min) {
        Date end = new Date();
        long consume = end.getTime() - begin.getTime();
        if (consume >= min) {
            if (PerformanceMonitor.useStdout) {
                System.out.println("PerformanceMonitor->" + serviceMethod + "花费" + consume + "毫秒." + formatter.format(begin) + "->" + formatter.format(end));
            } else {
                PerformanceMonitor.logger.info("PerformanceMonitor->{}花费{}毫秒.{}->{}", serviceMethod, consume, formatter.format(begin), formatter.format(end));
            }
        }
    }

    String getServiceMethod() {
        return serviceMethod;
    }

}
