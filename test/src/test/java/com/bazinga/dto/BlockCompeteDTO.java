package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockCompeteDTO {

    private Integer competeNum;

    private String blockName;

    private BigDecimal rate;


    public BlockCompeteDTO(Integer competeNum, String blockName, BigDecimal rate) {
        this.competeNum = competeNum;
        this.blockName = blockName;
        this.rate = rate;
    }
}
