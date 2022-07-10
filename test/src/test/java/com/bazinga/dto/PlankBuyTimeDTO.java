package com.bazinga.dto;


import lombok.Data;

@Data
public class PlankBuyTimeDTO {
    private String stockCode;
    private int planks;
    private Long start;
    private Long end;

}
