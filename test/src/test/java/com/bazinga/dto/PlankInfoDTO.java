package com.bazinga.dto;

import lombok.Data;

@Data
public class PlankInfoDTO {

    private String stockCode;

    private String plankTime;

    public PlankInfoDTO(String stockCode, String plankTime) {
        this.stockCode = stockCode;
        this.plankTime = plankTime;
    }
}
