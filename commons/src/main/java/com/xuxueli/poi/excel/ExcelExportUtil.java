//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xuxueli.poi.excel;

import com.xuxueli.poi.excel.annotation.ExcelField;
import com.xuxueli.poi.excel.annotation.ExcelSheet;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelExportUtil {
    private static Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);

    public ExcelExportUtil() {
    }

    public static Workbook exportWorkbook(List<?> dataList) {
        if (dataList != null && dataList.size() != 0) {
            Class<?> sheetClass = dataList.get(0).getClass();
            ExcelSheet excelSheet = (ExcelSheet)sheetClass.getAnnotation(ExcelSheet.class);
            String sheetName = dataList.get(0).getClass().getSimpleName();
            HSSFColorPredefined headColor = null;
            if (excelSheet != null) {
                if (excelSheet.name() != null && excelSheet.name().trim().length() > 0) {
                    sheetName = excelSheet.name().trim();
                }

                headColor = excelSheet.headColor();
            }

            List<Field> fields = new ArrayList();
            if (sheetClass.getDeclaredFields() != null && sheetClass.getDeclaredFields().length > 0) {
                Field[] var6 = sheetClass.getDeclaredFields();
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    Field field = var6[var8];
                    if (!Modifier.isStatic(field.getModifiers())) {
                        fields.add(field);
                    }
                }
            }

            if (fields != null && fields.size() != 0) {
                Workbook workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet(sheetName);
                CellStyle headStyle = null;
                if (headColor != null) {
                    headStyle = workbook.createCellStyle();
                    headStyle.setFillForegroundColor(headColor.getIndex());
                    headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    headStyle.setFillBackgroundColor(headColor.getIndex());
                }

                Row headRow = sheet.createRow(0);

                int dataIndex;
                for(dataIndex = 0; dataIndex < fields.size(); ++dataIndex) {
                    Field field = (Field)fields.get(dataIndex);
                    ExcelField excelField = (ExcelField)field.getAnnotation(ExcelField.class);
                    String fieldName = excelField != null && excelField.name() != null && excelField.name().trim().length() > 0 ? excelField.name() : field.getName();
                    Cell cellX = headRow.createCell(dataIndex, 1);
                    if (headStyle != null) {
                        cellX.setCellStyle(headStyle);
                    }

                    cellX.setCellValue(String.valueOf(fieldName));
                }

                for(dataIndex = 0; dataIndex < dataList.size(); ++dataIndex) {
                    int rowIndex = dataIndex + 1;
                    Object rowData = dataList.get(dataIndex);
                    Row rowX = sheet.createRow(rowIndex);

                    for(int i = 0; i < fields.size(); ++i) {
                        Field field = (Field)fields.get(i);

                        try {
                            field.setAccessible(true);
                            Object fieldValue = field.get(rowData);
                            Cell cellX = rowX.createCell(i, 1);
                            cellX.setCellValue(String.valueOf(fieldValue==null?"":fieldValue));
                        } catch (IllegalAccessException var18) {
                            logger.error(var18.getMessage(), var18);
                            throw new RuntimeException(var18);
                        }
                    }
                }

                return workbook;
            } else {
                throw new RuntimeException(">>>>>>>>>>> xxl-excel error, data field can not be empty.");
            }
        } else {
            throw new RuntimeException(">>>>>>>>>>> xxl-excel error, data can not be empty.");
        }
    }

    public static void exportToFile(List<?> dataList, String filePath) {
        Workbook workbook = exportWorkbook(dataList);
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);
            fileOutputStream.flush();
        } catch (Exception var11) {
            logger.error(var11.getMessage(), var11);
            throw new RuntimeException(var11);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception var12) {
                logger.error(var12.getMessage(), var12);
                throw new RuntimeException(var12);
            }

        }

    }

    public static byte[] exportToBytes(List<?> dataList) {
        Workbook workbook = exportWorkbook(dataList);
        ByteArrayOutputStream byteArrayOutputStream = null;
        Object var3 = null;

        byte[] var4;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byte[] result = byteArrayOutputStream.toByteArray();
            var4 = result;
        } catch (Exception var12) {
            logger.error(var12.getMessage(), var12);
            throw new RuntimeException(var12);
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (Exception var13) {
                logger.error(var13.getMessage(), var13);
                throw new RuntimeException(var13);
            }

        }

        return var4;
    }
}
