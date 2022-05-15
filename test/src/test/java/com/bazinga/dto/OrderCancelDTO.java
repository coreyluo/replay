package com.bazinga.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.util.Date;

@Data
public class OrderCancelDTO {


    @ExcelElement("stock_code")
    private String stockCode;

    @ExcelElement("status")
    private Integer status;

    @ExcelElement("create_time")
    private Date orderDate;

    @ExcelElement("update_time")
    private Date cancelDate;


}
