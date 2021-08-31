package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlockLevelDTO implements Comparable<BlockLevelDTO>{

    private String blockCode;
    private String blockName;
    private BigDecimal avgRate;
    private Integer level;

    public BlockLevelDTO(String blockCode, String blockName, BigDecimal avgRate) {
        this.blockCode = blockCode;
        this.blockName = blockName;
        this.avgRate = avgRate;
    }

    public BlockLevelDTO() {
    }


    @Override
    public int compareTo(BlockLevelDTO o) {
        if(o.getAvgRate()==null){
            return -1;
        }
        if(this.getAvgRate()==null){
            return 1;
        }
        if(o.getAvgRate().compareTo(this.getAvgRate())==1){
            return 1;
        }else if(o.getAvgRate().compareTo(this.getAvgRate())==-1){
            return -1;
        }else{
            return 0;
        }
    }

}
