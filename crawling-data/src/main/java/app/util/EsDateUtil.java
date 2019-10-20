package app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ES时间戳生成工具类
 *
 * @author faith.huan 2019-05-28 14:08
 */
public class EsDateUtil {

    private static final SimpleDateFormat SDF_8 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.CHINESE);

    /**
     * 根据传入date,返回ES所需日期字符串
     *
     * @param date 日期
     * @return es日期字符串
     */
    public static synchronized String getEsDateString(Date date) {
        return SDF_8.format(date);
    }

    /**
     * 获取当前日期对应的ES日期字符串
     *
     * @return es日期字符串
     */
    public static synchronized String getEsDateStringNow() {
        return SDF_8.format(new Date());
    }

}
