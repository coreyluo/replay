package com.tradex.util;


import com.tradex.enums.ExchangeId;
import com.tradex.tradex.TdxHqClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;


public class TdxHqPollManager {

    // 循环次数
    protected static final int LOOP_TIMES = 3;
    protected volatile static GenericObjectPool<TdxHqClient> TDX_HQ_CLIENT_POOL = null;

    /**
     * 方法介绍: 获取HttpConnection对象，如果有则给，没有则创建了再给</br>
     * 注意事项：无</br>
     * 创建日期: 2016年12月29日 下午6:39:50</br>
     *
     * -----------------------------------</br>
     * 修改原因:</br>
     * 修改日期:</br>
     * 修改人：</br>
     * -----------------------------------</br>
     *
     * @author： 罗成</br>
     * 
     * @return
     * @throws Exception
     */
    protected static TdxHqClient getTdxHqClient() throws Exception {
        TdxHqClient tdxHqClient = null;
        if (TDX_HQ_CLIENT_POOL != null) {
            tdxHqClient = TDX_HQ_CLIENT_POOL.borrowObject();
        } else {
            // 这里直接用对象锁即可，访问的网站数量是有限的，如果后续访问网站数量剧增，考虑用key去锁
            synchronized (TdxHqPollManager.class) {
                if (TDX_HQ_CLIENT_POOL != null) {
                    tdxHqClient = TDX_HQ_CLIENT_POOL.borrowObject();
                } else {
                    TdxHqPoolableObjectFactory factory = new TdxHqPoolableObjectFactory();
                    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
                    // 暂时默认最多3个 只能里连接一个 否则报错
                    config.setMaxTotal(3);
                    // 获取不到用于等待
                    config.setMaxWaitMillis(-1);
                    // 这里代表 如果超过5分钟就移除对象
                    config.setMinEvictableIdleTimeMillis(300000);
                    // 这里代表多久去检测一次移除对象,简单的说 这里最长就是60S+300S 一个对象空闲一定会被移除
                    config.setTimeBetweenEvictionRunsMillis(60000);
                    TDX_HQ_CLIENT_POOL = new GenericObjectPool<>(factory, config);
                    tdxHqClient = TDX_HQ_CLIENT_POOL.borrowObject();
                }
            }
        }
        return tdxHqClient;

    }

    /**
     * 方法介绍: 将HttpConnection还给线程池</br>
     * 注意事项：无</br>
     * 创建日期: 2016年12月29日 下午6:36:33</br>
     *
     * -----------------------------------</br>
     * 修改原因:</br>
     * 修改日期:</br>
     * 修改人：</br>
     * -----------------------------------</br>
     *
     * @author： 罗成</br>
     * 
     * @throws Exception
     */
    protected static void returnTdxHqClient(TdxHqClient tdxHqClient) throws Exception {
        if (TDX_HQ_CLIENT_POOL != null) {
            if (tdxHqClient != null) {
                TDX_HQ_CLIENT_POOL.returnObject(tdxHqClient);
            }
        }
    }

    /**
     * 方法介绍: 将HttpConnection销毁</br>
     * 注意事项：无</br>
     * 创建日期: 2016年12月29日 下午6:36:33</br>
     *
     * -----------------------------------</br>
     * 修改原因:</br>
     * 修改日期:</br>
     * 修改人：</br>
     * -----------------------------------</br>
     *
     * @author： 罗成</br>
     * 
     * @throws Exception
     */
    protected static void destroyTdxHqClient(TdxHqClient tdxHqClient) throws Exception {
        if (TDX_HQ_CLIENT_POOL != null) {
            if (tdxHqClient != null) {
                TDX_HQ_CLIENT_POOL.invalidateObject(tdxHqClient);
            }
        }
    }

    protected static Long getAccountIdByRoute(String stockCode) {
        ExchangeId exchangeId = StockUtils.getExchangeId(stockCode);
        if(exchangeId==null){
            return 1L;
        }
        switch (exchangeId){
            case SZ:
                return 1L;
            case SH:
                return 2L;
            default:
                return 1L;
        }
    }

    protected static Long getAccountIdByRoute(ExchangeId exchangeId) {
        switch (exchangeId){
            case SZ:
                return 1L;
            case SH:
                return 2L;
            default:
                return 1L;
        }
    }


}
