package com.bazinga.dto;


import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.util.Date;

@Data
public class UpAbsortImportDTO {

    @ExcelElement("kbarDate")
    private Date kbarDate;



}
