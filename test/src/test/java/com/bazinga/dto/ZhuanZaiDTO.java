package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

@Data
public class ZhuanZaiDTO {

    @ExcelElement("代码")
    private String stockCode;
}
