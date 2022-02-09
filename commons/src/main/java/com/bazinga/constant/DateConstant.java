package com.bazinga.constant;



import com.bazinga.util.DateUtil;

import java.util.Date;

public class DateConstant {

    public static final Date NINE_HALF_CLOCK;

    public static final Date NINE_THIRTY_FIVE_CLOCK;

    public static final Date NINE_THIRTY_ONE_CLOCK;

    public static final Date NINE_9_29_58;

    public static final Date NINE_9_30_05;

    public static final Date TEN_CLOCK;

    public static final Date THIRTEEN_HALF_LOCK;

    public static final Date AM_09_30_00;
    public static final Date AM_09_30_08;
    public static final Date AM_09_30_15;
    public static final Date AM_09_30_30;
    public static final Date AM_09_30_07;
    public static final Date AM_09_31_00;
    public static final Date AM_09_38_40;
    public static final Date AM_09_33_00;
    public static final Date AM_09_24_00;
    public static final Date AM_09_24_54;
    public static final Date AM_09_24_58;
    public static final Date AM_09_24_57;
    public static final Date AM_09_24_59;
    public static final Date AM_09_25_03;
    public static final Date AM_09_28_03;
    public static final Date AM_09_19_50;
    public static final Date AM_09_25_10;
    public static final Date AM_09_29_57;
    public static final Date AM_09_32_00;
    public static final Date AM_09_34_00;
    public static final Date AM_09_35_00;
    public static final Date AM_09_40_00;
    public static final Date AM_09_45_00;
    public static final Date AM_10_30_00;
    public static final Date AM_10_15_00;
    public static final Date AM_10_24_00;
    public static final Date AM_10_45_00;
    public static final Date AM_10_59_55;
    public static final Date AM_11_00_06;
    public static final Date AM_11_31_00;

    public static final Date PM_13_00_00;
    public static final Date PM_13_00_03;
    public static final Date PM_13_00_08;
    public static final Date PM_13_00_30;
    public static final Date PM_13_01_00;

    public static final Date AM_10_00_00;
    public static final Date AM_10_01_00;
    public static final Date PM_12_59_00;
    public static final Date PM_13_08_40;
    public static final Date PM_13_21_00;
    public static final Date PM_13_22_00;
    public static final Date PM_13_30_00;
    public static final Date AM_11_30_00;
    public static final Date AM_11_20_00;
    public static final Date PM_15_30_00;
    public static final Date PM_15_00_03;
    public static final Date PM_15_00_30;
    public static final Date PM_15_01_00;
    public static final Date PM_15_05_30;
    public static final Date PM_15_10_30;
    public static final Date PM_14_45_00;
    public static final Date PM_14_00_00;
    public static final Date PM_14_50_00;
    public static final Date PM_14_56_00;
    public static final Date PM_14_57_00;
    public static final Date PM_17_30_00;
    public static final String TODAY_STRING;


    static {
        String dayString = DateUtil.format(new Date(), DateUtil.yyyy_MM_dd);
        NINE_HALF_CLOCK = DateUtil.parseDate(dayString + " 09:29:30", DateUtil.DEFAULT_FORMAT);
        NINE_THIRTY_FIVE_CLOCK = DateUtil.parseDate(dayString + " 09:35:00", DateUtil.DEFAULT_FORMAT);
        NINE_THIRTY_ONE_CLOCK = DateUtil.parseDate(dayString + " 09:31:00", DateUtil.DEFAULT_FORMAT);
        NINE_9_29_58 = DateUtil.parseDate(dayString + " 09:29:58", DateUtil.DEFAULT_FORMAT);
        NINE_9_30_05 = DateUtil.parseDate(dayString + " 09:30:05", DateUtil.DEFAULT_FORMAT);
        TEN_CLOCK = DateUtil.parseDate(dayString + " 10:00:00", DateUtil.DEFAULT_FORMAT);
        THIRTEEN_HALF_LOCK = DateUtil.parseDate(dayString + " 13:30:00", DateUtil.DEFAULT_FORMAT);
        AM_09_30_00 = DateUtil.parseDate(dayString + " 09:30:00", DateUtil.DEFAULT_FORMAT);
        AM_09_30_08 = DateUtil.parseDate(dayString + " 09:30:08", DateUtil.DEFAULT_FORMAT);
        AM_09_30_15 = DateUtil.parseDate(dayString + " 09:30:15", DateUtil.DEFAULT_FORMAT);
        AM_09_30_30 = DateUtil.parseDate(dayString + " 09:30:30", DateUtil.DEFAULT_FORMAT);
        AM_09_30_07 = DateUtil.parseDate(dayString + " 09:30:07", DateUtil.DEFAULT_FORMAT);
        AM_09_31_00 = DateUtil.parseDate(dayString + " 09:31:00", DateUtil.DEFAULT_FORMAT);
        AM_09_38_40 = DateUtil.parseDate(dayString + " 09:38:40", DateUtil.DEFAULT_FORMAT);
        AM_10_30_00 = DateUtil.parseDate(dayString + " 10:30:00", DateUtil.DEFAULT_FORMAT);
        PM_13_00_00 = DateUtil.parseDate(dayString + " 13:00:00", DateUtil.DEFAULT_FORMAT);
        PM_13_00_03 = DateUtil.parseDate(dayString + " 13:00:03", DateUtil.DEFAULT_FORMAT);
        PM_13_00_08 = DateUtil.parseDate(dayString + " 13:00:08", DateUtil.DEFAULT_FORMAT);
        PM_13_00_30 = DateUtil.parseDate(dayString + " 13:00:30", DateUtil.DEFAULT_FORMAT);
        PM_13_01_00 = DateUtil.parseDate(dayString + " 13:01:00", DateUtil.DEFAULT_FORMAT);
        AM_10_00_00 = DateUtil.parseDate(dayString + " 10:00:00", DateUtil.DEFAULT_FORMAT);
        AM_10_01_00 = DateUtil.parseDate(dayString + " 10:01:00", DateUtil.DEFAULT_FORMAT);
        AM_10_15_00 = DateUtil.parseDate(dayString + " 10:15:00", DateUtil.DEFAULT_FORMAT);
        AM_10_24_00 = DateUtil.parseDate(dayString + " 10:24:00", DateUtil.DEFAULT_FORMAT);
        AM_10_45_00 = DateUtil.parseDate(dayString + " 10:45:00", DateUtil.DEFAULT_FORMAT);
        AM_10_59_55 = DateUtil.parseDate(dayString + " 10:59:50", DateUtil.DEFAULT_FORMAT);
        AM_11_00_06 = DateUtil.parseDate(dayString + " 11:00:06", DateUtil.DEFAULT_FORMAT);
        PM_12_59_00 = DateUtil.parseDate(dayString + " 12:59:00", DateUtil.DEFAULT_FORMAT);
        PM_13_08_40 = DateUtil.parseDate(dayString + " 13:08:40", DateUtil.DEFAULT_FORMAT);
        PM_13_21_00 = DateUtil.parseDate(dayString + " 13:21:00", DateUtil.DEFAULT_FORMAT);
        PM_13_22_00 = DateUtil.parseDate(dayString + " 13:22:00", DateUtil.DEFAULT_FORMAT);
        PM_13_30_00 = DateUtil.parseDate(dayString + " 13:30:00", DateUtil.DEFAULT_FORMAT);
        AM_11_20_00 = DateUtil.parseDate(dayString + " 11:20:00", DateUtil.DEFAULT_FORMAT);
        AM_11_30_00 = DateUtil.parseDate(dayString + " 11:30:00", DateUtil.DEFAULT_FORMAT);
        AM_11_31_00 = DateUtil.parseDate(dayString + " 11:31:00", DateUtil.DEFAULT_FORMAT);
        PM_15_30_00 = DateUtil.parseDate(dayString + " 15:30:00", DateUtil.DEFAULT_FORMAT);
        AM_09_33_00 = DateUtil.parseDate(dayString + " 09:33:00", DateUtil.DEFAULT_FORMAT);
        AM_09_24_54 = DateUtil.parseDate(dayString + " 09:24:54", DateUtil.DEFAULT_FORMAT);
        AM_09_24_58 = DateUtil.parseDate(dayString + " 09:24:58", DateUtil.DEFAULT_FORMAT);
        AM_09_24_57 = DateUtil.parseDate(dayString + " 09:24:57", DateUtil.DEFAULT_FORMAT);
        AM_09_24_59 = DateUtil.parseDate(dayString + " 09:24:59", DateUtil.DEFAULT_FORMAT);
        AM_09_25_03 = DateUtil.parseDate(dayString + " 09:25:03", DateUtil.DEFAULT_FORMAT);
        AM_09_28_03 = DateUtil.parseDate(dayString + " 09:28:03", DateUtil.DEFAULT_FORMAT);
        AM_09_24_00 = DateUtil.parseDate(dayString + " 09:24:00", DateUtil.DEFAULT_FORMAT);
        AM_09_19_50 = DateUtil.parseDate(dayString + " 09:19:50", DateUtil.DEFAULT_FORMAT);
        AM_09_25_10 = DateUtil.parseDate(dayString + " 09:25:10", DateUtil.DEFAULT_FORMAT);
        AM_09_29_57 = DateUtil.parseDate(dayString + " 09:29:57", DateUtil.DEFAULT_FORMAT);
        AM_09_32_00 = DateUtil.parseDate(dayString + " 09:32:00", DateUtil.DEFAULT_FORMAT);
        AM_09_34_00 = DateUtil.parseDate(dayString + " 09:34:00", DateUtil.DEFAULT_FORMAT);
        AM_09_35_00 = DateUtil.parseDate(dayString + " 09:35:00", DateUtil.DEFAULT_FORMAT);
        PM_15_00_03 = DateUtil.parseDate(dayString + " 15:30:03", DateUtil.DEFAULT_FORMAT);
        PM_15_00_30 = DateUtil.parseDate(dayString + " 15:00:30", DateUtil.DEFAULT_FORMAT);
        PM_15_01_00 = DateUtil.parseDate(dayString + " 15:01:00", DateUtil.DEFAULT_FORMAT);
        PM_15_05_30 = DateUtil.parseDate(dayString + " 15:05:30", DateUtil.DEFAULT_FORMAT);
        PM_15_10_30 = DateUtil.parseDate(dayString + " 15:10:30", DateUtil.DEFAULT_FORMAT);
        PM_14_45_00 = DateUtil.parseDate(dayString + " 14:45:00", DateUtil.DEFAULT_FORMAT);
        PM_14_00_00 = DateUtil.parseDate(dayString + " 14:00:00", DateUtil.DEFAULT_FORMAT);
        PM_14_50_00 = DateUtil.parseDate(dayString + " 14:50:00", DateUtil.DEFAULT_FORMAT);
        PM_14_56_00 = DateUtil.parseDate(dayString + " 14:56:00", DateUtil.DEFAULT_FORMAT);
        PM_14_57_00 = DateUtil.parseDate(dayString + " 14:57:00", DateUtil.DEFAULT_FORMAT);
        AM_09_40_00 = DateUtil.parseDate(dayString + " 09:40:00", DateUtil.DEFAULT_FORMAT);
        AM_09_45_00 = DateUtil.parseDate(dayString + " 09:45:00", DateUtil.DEFAULT_FORMAT);
        PM_17_30_00 = DateUtil.parseDate(dayString + " 17:30:00", DateUtil.DEFAULT_FORMAT);
        TODAY_STRING = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
    }
}
