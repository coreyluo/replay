package com.bazinga.base;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈分表分库工具类〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 18/11/28
 */
public abstract class ShardingUtils {

    private static final String CREATE_TEMPLATE = "create table %s_%s like %s;";

    /**
     * 取余算法分表，获取分表序号
     * @param id
     * @param tables    表数
     * @return 表序号
     */
    public static int getTableSeqByMod(int id, int tables){
        return id & (tables-1);
    }

    /**
     * 取余算法分表，获取分表序号
     * @param id
     * @param tables    表数
     * @return 表序号
     */
    public static long getTableSeqByMod(long id, int tables){
        return id & (tables-1);
    }

    /**
     * 取余虚拟表算法分表，获取表序号
     * @param id
     * @param maxTables     最大表数
     * @param actualTables  实际表数
     * @return 表序号
     */
    public static int getTableSeqByMod(int id, int maxTables, int actualTables){
        checkTableStep(maxTables, actualTables);

        int tableStep = maxTables/actualTables;
        return id & (maxTables-1) / tableStep * tableStep;
    }

    /**
     * 取余虚拟表算法分表，获取分表序号
     * @param id
     * @param maxTables     最大表数
     * @param actualTables  实际表数
     * @return 表序号
     */
    public static long getTableSeqByMod(long id, int maxTables, int actualTables){
        checkTableStep(maxTables, actualTables);

        int tableStep = maxTables/actualTables;
        return id & (maxTables-1) / tableStep * tableStep;
    }

    /**
     * 虚拟表算法，虚表序号转实表序号
     * @param virtualSeq    虚表序号
     * @param maxTables     最大表数
     * @param actualTables  实际表数
     * @return
     */
    public static int virtualSeqToActualSeq(int virtualSeq, int maxTables, int actualTables){
        checkTableStep(maxTables, actualTables);

        return virtualSeqToActualSeq(virtualSeq, maxTables/actualTables);
    }

    /**
     * 虚拟表算法，虚表序号转实表序号
     * @param virtualSeq    虚表序号
     * @param tableStep     表步进
     * @return
     */
    public static int virtualSeqToActualSeq(int virtualSeq, int tableStep){
        return virtualSeq / tableStep * tableStep;
    }

    /**
     * 分表建表语句
     * @param tableName
     * @param maxTables
     * @param actualTables
     * @param postfixLength
     */
    public static List<String> createTablesSql(String tableName, int maxTables, int actualTables, int postfixLength){
        checkTableStep(maxTables, actualTables);

        List<String> sqlList = new ArrayList<String>();
        int tableStep = maxTables/actualTables;
        for(int index=0; index<maxTables; index += tableStep){
            String postFix = StringUtils.leftPad(String.valueOf(index), postfixLength, '0');
            String createSql = String.format(CREATE_TEMPLATE, tableName, postFix, tableName);
            sqlList.add(createSql);
            System.out.println(createSql);
        }
        return sqlList;
    }

    private static void checkTableStep(int maxTables, int actualTables){
        if(maxTables % actualTables != 0){
            throw new IllegalArgumentException("最大表数必须为实际表数的整数倍，maxTables="+maxTables+", actualTables="+actualTables);
        }
    }

    /**
     * 获取指定宽度的分表序号字符串
     * @param id
     * @param maxTableNum
     * @param tableSeqLength
     * @return
     */
    public static String getTableSeqPostfix(long id, int maxTableNum, int tableSeqLength){
        return formatWith0(getTableSeqByMod(id, maxTableNum), tableSeqLength);
    }

    /**
     * 序号格式化为后缀
     * 1 -> "01"
     * @param seq
     * @param seqLength
     * @return
     */
    public static String formatWith0(long seq, int seqLength){
        return StringUtils.leftPad(String.valueOf(seq), seqLength, '0');
    }

    /**************************************** 分库相关 ****************************************/

    /**
     * 取余算法分库，获取分库序号
     * @param id
     * @param maxTableNum
     * @param maxDbNum
     * @return
     */
    public static int getDbSeq(int id, int maxTableNum, int maxDbNum){
        return id / maxTableNum & (maxDbNum-1);
    }

    /**
     * 取余算法分库，获取分库序号
     * @param id
     * @param maxTableNum
     * @param maxDbNum
     * @return
     */
    public static long getDbSeq(long id, int maxTableNum, int maxDbNum){
        return id / maxTableNum & (maxDbNum-1);
    }

    /**
     * 获取指定宽度的分库序号字符串
     * @param id
     * @param maxTableNum
     * @param maxDbNum
     * @param dbSeqLength
     * @return
     */
    public static String getDbSeqPostfix(long id, int maxTableNum, int maxDbNum, int dbSeqLength) {
        return formatWith0(getDbSeq(id, maxTableNum, maxDbNum), dbSeqLength);
    }

    public static void main(String[] args) {
        Assert.isTrue(getTableSeqByMod(3, 4) == 3, "");
        Assert.isTrue(getTableSeqByMod(5, 8, 4) == 4, "");

        Assert.isTrue(getTableSeqPostfix(3, 4, 2).equals("03"), "");

        Assert.isTrue(getDbSeq(19, 8, 8) == 2, "");
        Assert.isTrue(getDbSeqPostfix(19, 8, 8, 2).equals("02"), "");
    }

}
