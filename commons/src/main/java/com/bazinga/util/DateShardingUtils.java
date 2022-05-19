package com.bazinga.util;


import java.util.Date;

/**
 * 〈按时间分表工具〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 18/12/18
 */
public class DateShardingUtils {

    /**
     * 年月格式
     */
    private static String YEAR_MONTH = "yyyyMM";

    /**
     * 年格式
     */
    private static String YEAR = "yyyy";

    /**
     * 季度格式
     */
    private static String QUARTER = "q";

    /**
     * 获取年+季度
     *
     * @return
     */
    public static String getYearMonth(Date date) {
        return DateFormatUtils.format(date, YEAR_MONTH);
    }

    /**
     * 获取年+季度
     *
     * @return
     */


}
