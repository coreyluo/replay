package com.bazinga.replay.service;

import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.query.StockCommonReplayQuery;

import java.util.List;

/**
 * ��StockCommonReplay Service��<p>
 * ��������ϸ������
 *
 * @author
 * @date 2021-08-15
 */
public interface StockCommonReplayService {

    /**
     * ����һ����¼
     *
     * @param record �������
     */
    StockCommonReplay save(StockCommonReplay record);

    /**
     * ����ID��ѯ
     *
     * @param id ���ݿ�ID
     */
    StockCommonReplay getById(Long id);

    /**
     * ����id����һ������
     *
     * @param record ���²���
     */
    int updateById(StockCommonReplay record);

    /**
     * ���ݲ�ѯ������ѯ����
     *
     * @param query ��ѯ����
     */
    List<StockCommonReplay> listByCondition(StockCommonReplayQuery query);

    /**
     * ���ݲ�ѯ������ѯ��������
     *
     * @param query ��ѯ����
     */
    int countByCondition(StockCommonReplayQuery query);

    /**
     * Ψһ��uniqueKey ��ѯ
     *
     * @param uniqueKey ��ѯ����
     */
    StockCommonReplay getByUniqueKey(String uniqueKey);

    /**
     * Ψһ��uniqueKey ����
     *
     * @param record ���²���
     */
    int updateByUniqueKey(StockCommonReplay record);
}