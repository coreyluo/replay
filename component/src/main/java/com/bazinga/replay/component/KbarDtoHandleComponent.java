package com.bazinga.replay.component;

import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class KbarDtoHandleComponent {

    //包括新股最后一个一字板
    public List<KBarDTO> deleteNewStockTimes(List<KBarDTO> list, int size, String stockCode){
        List<KBarDTO> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        if(list.size()<size){
            KBarDTO firstDTO = null;
            BigDecimal preEndPrice = null;
            int i = 0;
            for (KBarDTO dto:list){
                if(preEndPrice!=null&&i==0){
                    boolean endPlank = PriceUtil.isUpperPrice(dto.getEndPrice(), preEndPrice);
                    if(MarketUtil.isChuangYe(stockCode)&&!dto.getDate().before(DateUtil.parseDate("2020-08-24",DateUtil.yyyy_MM_dd))){
                        endPlank = PriceUtil.isUpperPrice(stockCode,dto.getEndPrice(),preEndPrice);
                    }
                    if(!(dto.getHighestPrice().equals(dto.getLowestPrice())&&endPlank)){
                        datas.add(firstDTO);
                        i++;
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }

                if(i==0){
                    firstDTO = dto;
                }
                preEndPrice = dto.getEndPrice();
            }
        }else{
            return list;
        }
        return datas;
    }


    //不包括新股最后一个一字板
    public List<KBarDTO> deleteNewStockAllTimes(List<KBarDTO> list,int size,String stockCode){
        List<KBarDTO> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        if(list.size()<size){
            KBarDTO firstDTO = null;
            BigDecimal preEndPrice = null;
            int i = 0;
            for (KBarDTO dto:list){
                if(preEndPrice!=null&&i==0){
                    boolean endPlank = PriceUtil.isUpperPrice(dto.getEndPrice(), preEndPrice);
                    if(MarketUtil.isChuangYe(stockCode)&&!dto.getDate().before(DateUtil.parseDate("2020-08-24",DateUtil.yyyy_MM_dd))){
                        endPlank = PriceUtil.isUpperPrice(stockCode,dto.getEndPrice(),preEndPrice);
                    }
                    if(!(dto.getHighestPrice().equals(dto.getLowestPrice())&&endPlank)){
                        i++;
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }

                if(i==0){
                    firstDTO = dto;
                }
                preEndPrice = dto.getEndPrice();
            }
        }else{
            return list;
        }
        return datas;
    }


}
