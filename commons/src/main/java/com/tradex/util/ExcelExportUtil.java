package com.tradex.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author dw
 * @Description 导出excel util
 * @Date 2020/1/17 10:03
 * @Param
 * @return
 */

public class ExcelExportUtil {

    /**
     * Workbook对象
     */
    private Workbook workbook;
    /**
     * 工作表对象
     */
    private Sheet sheet;
    /**
     * 表大标题
     */
    private String title;
    /**
     * sheet各个列的表头
     */
    private String[] headList;
    /**
     * 各个列的表头对应的key值
     */
    private String[] headKey;
    /**
     * sheet需要填充的数据信息
     */
    private List<Map> data;
    /**
     * 工作表列宽
     */
    private Integer columnWidth = 20;
    /**
     * 工作表行高
     */
    private Integer rowHeight = 10;
    /**
     * 字体大小
     */
    private int fontSize = 14;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
    }

    public Integer getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(Integer rowHeight) {
        this.rowHeight = rowHeight;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void setSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    public String[] getHeadKey() {
        return headKey;
    }

    public void setHeadKey(String[] headKey) {
        this.headKey = headKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getHeadList() {
        return headList;
    }

    public void setHeadList(String[] headList) {
        this.headList = headList;
    }

    public List<Map> getData() {
        return data;
    }

    public void setData(List<Map> data) {
        this.data = data;
    }


    /**
     * 插入数据到表格（body）
     * @param startRow 开始行
     */
    public void writeMainData(Integer startRow) {
        //开始写入实体数据信息
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                Row row = this.sheet.createRow(startRow);
                Map map = data.get(i);
                for (int j = 0; j < headKey.length; j++) {
                    Cell cell = row.createCell(j);
                    //设置单个单元格的字体颜色
                    Object value = map.get(headKey[j]);
                    if(value ==null){
                        value = map.get(headKey[j-1]);
                        map.put(headKey[j],value);
                    }
                    if (null == value) {
                        String valueN = "";
                        cell.setCellValue(valueN);
                    } else if (value instanceof Integer) {
                        Integer valueInt = Integer.valueOf(value.toString());
                        cell.setCellValue(valueInt);
                    } else if (value instanceof String) {
                        String valueStr = String.valueOf(value);
                        cell.setCellValue(valueStr);
                    }else if(value instanceof BigDecimal){
                        BigDecimal bigDecimal = (BigDecimal) value;
                        cell.setCellValue(bigDecimal.toString());
                    }
                }
                startRow++;
            }
        }
    }


  /*  *//**
     * 写入表格大标题
     * @throws IOException
     *//*
    public void writeTitle() throws IOException {
        checkConfig();
        // 设置默认行宽
        this.sheet.setDefaultColumnWidth(20);
        // 在第0行创建rows  (表标题)
        Row title = this.sheet.createRow(0);
        // 设置行高
        title.setHeightInPoints(this.rowHeight);
        Cell cell = title.createCell(0);
        cell.setCellValue(this.title);
        CellStyle cellStyle = ExcelStyleUtilFor2003.getTitleStyle(this.workbook, true, 16);
        cell.setCellStyle(cellStyle);
        ExcelStyleUtilFor2003.mergeCell(sheet, 0, 0, 0, (this.headList.length - 1));
    }*/


    /**
     * 添加表头
     * @param head 表头字段[]
     * @param cellStyle 表头样式
     * @param headRowNum 表头开始行
     */
    public void writeTableHead(String[] head, CellStyle cellStyle, Integer headRowNum) {
        Row row = this.sheet.createRow(headRowNum);
        if (head.length > 0) {
            for (int i = 0; i < head.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(head[i]);
                cell.setCellStyle(cellStyle);
            }
        }
    }


    /**
     * 检查数据配置问题
     * @throws IOException 抛出数据异常类
     */
    protected void checkConfig() throws IOException {
        if (headKey == null || headList.length == 0) {
            throw new IOException("列名数组不能为空或者为NULL");
        }
        if (fontSize < 0) {
            throw new IOException("字体不能为负值");
        }
    }

    /**
     * 写入拼接好的数据，不需要通过表头key值来对应
     * @param dataLists 数据
     * @param startRow 开始行
     */
    public void writeMainData(List<List<String>> dataLists, Integer startRow) {
        if (CollectionUtils.isNotEmpty(dataLists)) {
            for (List<String> data : dataLists) {
                Row row = this.sheet.createRow(startRow);
                for (int i = 0; i < data.size(); i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(data.get(i));
                    CellStyle cellStyle = ExcelStyleUtilFor2003.setCellBorder(this.workbook);
                    cell.setCellStyle(cellStyle);
                }
                startRow++;
            }
        }
    }

    /**
     * 创建工作簿
     */
    public static Workbook creatWorkBook(String excelEnum){
        Workbook workbook = null;
        if (excelEnum.equals("XLS")) {
            // 2003
            workbook = new HSSFWorkbook();
        } else if (excelEnum.equals("XLSX")) {
            // 2007 及2007以上
            workbook = new XSSFWorkbook();
        }
        return workbook;
    }


}