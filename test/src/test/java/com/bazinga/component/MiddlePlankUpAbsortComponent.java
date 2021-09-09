package com.bazinga.component;

import com.bazinga.dto.PositionOwnImportDTO;
import com.bazinga.dto.UpAbsortImportDTO;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.Excel2JavaPojoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@Slf4j
public class MiddlePlankUpAbsortComponent {

    @Autowired
    private StockKbarService stockKbarService;

    public void replay(){
        File file = new File("E:/excelExport/中位板包括炸板20210907.xlsx");
        try {
            List<UpAbsortImportDTO> importList = new Excel2JavaPojoUtil(file).excel2JavaPojo(UpAbsortImportDTO.class);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
