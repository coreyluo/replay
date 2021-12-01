package com.bazinga.replay.component;


import com.bazinga.constant.CommonConstant;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.dto.PositionCalDTO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class AccountPositionCalComponent {

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private CommonComponent commonComponent;


    public void cal(){
        Date currentTradeDate = commonComponent.getCurrentTradeDate();
        String kbarDate = DateUtil.format(currentTradeDate,DateUtil.yyyyMMdd);
        try {

            File file = new File("E:\\positionCal\\398000086400_持仓_20211130.csv");
            List<String> list = FileUtils.readLines(file, "GBK");
            for (int i = 0; i < 3; i++) {
                list.remove(0);
            }
            log.info("");

            File ordeFile = new File("E:\\positionCal\\398000086400_委托_20211130.csv");
            List<String> orderList = FileUtils.readLines(ordeFile, "GBK");
            for (int i = 1; i < orderList.size(); i++) {

                String objectString = orderList.get(i);
                String[] objArr = objectString.split(SymbolConstants.COMMA);
                String direction = objArr[3];
                String tradeTime = objArr[16];
                if("-".equals(tradeTime)){
                    continue;
                }
                if("买入".equals(direction)){
                    PositionCalDTO positionCalDTO = new PositionCalDTO();
                    positionCalDTO.setTradeDate(DateUtil.format(currentTradeDate,DateUtil.slashDateFormat));
                    positionCalDTO.setOrderTime(objArr[0]);
                    positionCalDTO.setStockCode(objArr[1]);
                    positionCalDTO.setStockName(objArr[2]);
                    BigDecimal orderPrice = new BigDecimal(objArr[4]);
                    positionCalDTO.setTradeTime(tradeTime);
                    Date orderDate = DateUtil.parseDate(positionCalDTO.getOrderTime(), DateUtil.HH_MM_SS);
                    Date tradeDate = DateUtil.parseDate(positionCalDTO.getTradeTime(), DateUtil.HH_MM_SS);
                    long subTimeLong = tradeDate.getTime() - orderDate.getTime();
                    long hour = subTimeLong/(1000*60*60);
                    long min = subTimeLong%(1000*60*60)/(1000*60);
                    long second = subTimeLong%(1000*60)/(1000);

                    String subtractTime = hour + ":" +(min>9?min:"0"+min)+":"+ (second>9?second:"0"+second);
                    positionCalDTO.setSubtractTime(subtractTime);

                    String uniqueKey = positionCalDTO.getStockCode() +SymbolConstants.UNDERLINE + kbarDate;
                    StockKbar byUniqueKey = stockKbarService.getByUniqueKey(uniqueKey);
                    if(byUniqueKey ==null){
                        throw new Exception();
                    }
                    Integer sealType = byUniqueKey.getClosePrice().compareTo(orderPrice)==0?1:0;
                    positionCalDTO.setSealType(sealType);


                }



            }
            log.info("");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
