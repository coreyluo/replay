package com.bazinga.replay.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PlankExchangeMoneyDTO {
    private String tradeDateStr;
    private Date tradeDate;
    private long plank150UpMoney;
    private long plank150UpAmount;
    private long plank150DownMoney;
    private long plank150DownAmount;

    private long noPlank150UpMoney;
    private long noPlank150UpAmount;
    private long noPlank150DownMoney;
    private long noPlank150DownAmount;


    private long plankEnd150DownMoney;
    private long plankEnd150DownAmount;
    private long plankEnd150UpMoney;
    private long plankEnd150UpAmount;

    private long plankHigh300Count;
    private long plankEnd300Count;
    private long plankHigh300Money;
    private long plankEnd300Money;

    private long tenRate300Money;

}
