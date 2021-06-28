package com.bazinga.replay.convert;

import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.google.common.collect.Lists;
import com.tradex.model.suport.DataTable;

import java.math.BigDecimal;
import java.util.List;

public class ThirdSecondTransactionDataDTOConvert {

    public static List<ThirdSecondTransactionDataDTO> convert(DataTable dataTable){
        int rows = dataTable.rows();
        List<ThirdSecondTransactionDataDTO> transactionDataDTOList = Lists.newArrayList();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            ThirdSecondTransactionDataDTO dto = new ThirdSecondTransactionDataDTO();
            dto.setTradeTime(row[0]);
            dto.setTradePrice(new BigDecimal(row[1]).setScale(2, BigDecimal.ROUND_HALF_UP));
            dto.setTradeQuantity(Integer.parseInt(row[2]));
            dto.setTradeType(Integer.parseInt(row[3]));
            transactionDataDTOList.add(dto);
        }
        return transactionDataDTOList;

    }


}
