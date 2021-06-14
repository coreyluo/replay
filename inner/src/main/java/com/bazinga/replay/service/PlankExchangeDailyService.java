package com.bazinga.replay.service;

import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.query.PlankExchangeDailyQuery;

import java.util.List;

/**
 * ��PlankExchangeDaily Service��<p>
 * ��������ϸ������
 *
 * @author
 * @date 2021-06-14
 */
public interface PlankExchangeDailyService {

    /**
     * ����һ����¼
     *
     * @param record �������
     */
    PlankExchangeDaily save(PlankExchangeDaily record);

    /**
     * ����ID��ѯ
     *
     * @param id ���ݿ�ID
     */
    PlankExchangeDaily getById(Long id);

    /**
     * ����id����һ������
     *
     * @param record ���²���
     */
    int updateById(PlankExchangeDaily record);

    /**
     * ���ݲ�ѯ������ѯ����
     *
     * @param query ��ѯ����
     */
    List<PlankExchangeDaily> listByCondition(PlankExchangeDailyQuery query);

    /**
     * ���ݲ�ѯ������ѯ��������
     *
     * @param query ��ѯ����
     */
    int countByCondition(PlankExchangeDailyQuery query);
}