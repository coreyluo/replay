package com.bazinga.dto;


import lombok.Data;

import java.util.List;

@Data
public class PlankTimePairDTO {
    private String stockCode;
    private int planks;
    private Long start;
    private Long end;

}
