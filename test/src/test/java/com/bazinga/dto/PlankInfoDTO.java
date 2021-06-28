package com.bazinga.dto;

import lombok.Data;

@Data
public class PlankInfoDTO {

    private String stockCode;

    private String plankTime;

    private Integer maxDragonNum;

    private String blockCode;

    public PlankInfoDTO(String stockCode, String plankTime) {
        this.stockCode = stockCode;
        this.plankTime = plankTime;
    }

    public PlankInfoDTO() {
    }
}
