package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LevelDTO  implements Comparable<LevelDTO>{

    private String key;

    private BigDecimal rate;


    @Override
    public int compareTo(LevelDTO o) {
        if(o.getRate()==null){
            return -1;
        }
        if(this.getRate()==null){
            return 1;
        }
        if(o.getRate().compareTo(this.getRate())==1){
            return 1;
        }else if(o.getRate().compareTo(this.getRate())==-1){
            return -1;
        }else{
            return 0;
        }
    }


}
