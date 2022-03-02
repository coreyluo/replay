package com.bazinga.replay.component;



import com.bazinga.enums.MarketTypeEnum;
import com.bazinga.replay.convert.CiculateInfoDTOConvert;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.CirculateInfoDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.NewStock;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.NewStockService;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.tradex.enums.ExchangeId;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Component
@Slf4j
public class NewStockComponent {
    @Autowired
    private NewStockService newStockService;
    @Autowired
    private CirculateInfoService circulateInfoService;

    public void catchNewStock(){
        for (int i = 0;i<=30;i++) {
            int size = 1000;
            int start = i *size;
            DataTable dataTable = null;
            try {
                dataTable = TdxHqUtil.getSecurityList(ExchangeId.SH, start, size);
            }catch (Exception e){
                log.info("获取市场所有股票超出边界了");
            }
            List<CirculateInfoDTO> list = CiculateInfoDTOConvert.convert(dataTable);
            for (CirculateInfoDTO dto:list){
                if(dto.getStock().startsWith("60")||dto.getStock().startsWith("688")||dto.getStock().startsWith("689")){
                    isShangHaiNewStock(dto.getStock(),dto.getStockName());
                }
            }
        }

        for (int i = 0;i<=30;i++) {
            int size = 1000;
            int start = i *size;
            DataTable dataTable = null;
            try {
                dataTable = TdxHqUtil.getSecurityList(ExchangeId.SZ, start, size);
            }catch (Exception e){
                log.info("获取市场所有股票超出边界了");
            }
            List<CirculateInfoDTO> list = CiculateInfoDTOConvert.convert(dataTable);
            for (CirculateInfoDTO dto:list){
                if(dto.getStock().startsWith("30")||dto.getStock().startsWith("00")){
                    isShengZhenNewStock(dto.getStock(),dto.getStockName());
                }
            }
        }
    }

    public void isShangHaiNewStock(String stockCode,String stockName){
        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 50);
        List<KBarDTO> kBarDTOS = KBarDTOConvert.convertKBar(securityBars);
        boolean isNew = true;
        if(!CollectionUtils.isEmpty(kBarDTOS)&&kBarDTOS.size()<50){
            if(kBarDTOS.get(0).getTotalExchange()==null||kBarDTOS.get(0).getTotalExchange()==0){
                isNew=false;
            }
            if(stockCode.startsWith("60")){
                int i = 0;
                BigDecimal preEndPrice = null;
                for (KBarDTO dto:kBarDTOS){
                    i++;
                    if(i==1){
                        if(!dto.getHighestPrice().equals(dto.getEndPrice())){
                            isNew = false;
                        }
                    }
                    if(i!=1){
                        boolean upperPrice = PriceUtil.isUpperPrice(stockCode, dto.getEndPrice(), preEndPrice);
                        if(!dto.getHighestPrice().equals(dto.getLowestPrice())){
                            isNew = false;
                        }
                        if(!upperPrice){
                            isNew = false;
                        }
                    }
                    preEndPrice = dto.getEndPrice();
                }
            }
            if(stockCode.startsWith("688")||stockCode.startsWith("689")){
                if(kBarDTOS.size()>5){
                    isNew = false;
                }
            }
        }else{
            isNew = false;
        }
        if(isNew){
            KBarDTO kBarDTO = kBarDTOS.get(0);
            Date marketDate = kBarDTO.getDate();
            saveKbarDto(stockCode,stockName,marketDate);
        }
    }

    public void isShengZhenNewStock(String stockCode,String stockName){
        DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, stockCode, 0, 50);
        List<KBarDTO> kBarDTOS = KBarDTOConvert.convertKBar(securityBars);
        boolean isNew = true;
        if(!CollectionUtils.isEmpty(kBarDTOS)&&kBarDTOS.size()<50){
            if(kBarDTOS.get(0).getTotalExchange()==null||kBarDTOS.get(0).getTotalExchange()==0){
                isNew = false;
            }
            if(!stockCode.startsWith("30")){
                int i = 0;
                BigDecimal preEndPrice = null;
                for (KBarDTO dto:kBarDTOS){
                    i++;
                    if(i==1){
                        if(!dto.getHighestPrice().equals(dto.getEndPrice())){
                            isNew = false;
                        }
                    }
                    if(i!=1){
                        boolean upperPrice = PriceUtil.isUpperPrice(stockCode, dto.getEndPrice(), preEndPrice);
                        if(!dto.getHighestPrice().equals(dto.getLowestPrice())){
                            isNew = false;
                        }
                        if(!upperPrice){
                            isNew = false;
                        }
                    }
                    preEndPrice = dto.getEndPrice();
                }
            }

            if(stockCode.startsWith("30")){
                if(kBarDTOS.size()<5){
                    isNew = false;
                }
                if(kBarDTOS.size()>=5&&kBarDTOS.size()<50){
                    isNew = true;
                }
            }
        }else{
            isNew = false;
        }
        if(isNew){
            KBarDTO kBarDTO = kBarDTOS.get(0);
            Date marketDate = kBarDTO.getDate();
            saveKbarDto(stockCode,stockName,marketDate);
        }
    }

    public void saveKbarDto(String stockCode,String stockName,Date marketDate){
        if(!MarketUtil.isKeChuang(stockCode)) {
            try {
                NewStock newStock = new NewStock();
                newStock.setStockCode(stockCode);
                newStock.setStockName(stockName);
                newStock.setMarketDate(marketDate);
                newStock.setCreateTime(new Date());
                newStockService.save(newStock);
                CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
                circulateInfoQuery.setStockCode(stockCode);
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
                if (CollectionUtils.isEmpty(circulateInfos)) {
                    CirculateInfo circulateInfo = new CirculateInfo();
                    circulateInfo.setStockCode(stockCode);
                    circulateInfo.setStockName(stockName);
                    circulateInfo.setCirculate(100000000l);
                    circulateInfo.setCirculateZ(100000000l);
                    circulateInfo.setCreateTime(new Date());
                    circulateInfo.setMarketType(MarketTypeEnum.GENERAL.getCode());
                    circulateInfo.setStockType(1);
                    circulateInfoService.save(circulateInfo);
                    log.info("添加新股数据stockCode：{}", stockCode);
                }
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }

}
