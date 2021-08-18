package com.tradex.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @Author
 * @ClassName ExcelStyleUtilFor2003
 * @Description excel样式
 * @Date 2019/7/26 14:16
 * @Version 1.0
 */
public class ExcelStyleUtilFor2003 {


    /**
     * 样式居中
     * @param cellStyle
     */
    public static void center(CellStyle cellStyle) {
        // 水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }


    /**
     * 单元格合并
     * @param wbSheet 工作表对象
     * @param firstRow 合并的开始行
     * @param lastRow 合并的结束行
     * @param firstCol 合并的开始列
     * @param lastCol 合并的结束列
     */
    public static void mergeCell(Sheet wbSheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        wbSheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }


    /**
     * 加粗，垂直居中
     * @param wb
     * @param isBold
     * @param FontSize
     * @return
     */
    public static CellStyle getTitleStyle(Workbook wb, Boolean isBold, int FontSize) {
        // 标题样式（加粗，垂直居中）
        CellStyle cellStyle = wb.createCellStyle();
        center(cellStyle);
        Font font = wb.createFont();
        // 加粗
        font.setBold(isBold);
        // 设置标题字体大小
        font.setFontHeightInPoints((short) FontSize);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 表头样式
     * @param wb
     * @param fontSize
     * @return
     */
    public static CellStyle getHeadStyle(Workbook wb, int fontSize) {
        CellStyle cellStyle = wb.createCellStyle();
        // 设置表头的背景颜色
        cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置表头的背景颜色
        cellStyle.setFillBackgroundColor(IndexedColors.BLUE.getIndex());
        center(cellStyle);
        //设置字体
        Font font = setFont(wb, fontSize);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * body通用样式: 居中，设置字体大小
     * @param wb
     * @param fontSize
     * @return
     */
    public static CellStyle getBodyStyle(Workbook wb, int fontSize) {
        CellStyle cellStyle = wb.createCellStyle();
        //设置单元格样式
        center(cellStyle);
        Font font = setFont(wb, fontSize);
        cellStyle.setFont(font);
        return cellStyle;
    }


    /**
     * 自动换行样式
     * @param wb
     * @return
     */
    public static CellStyle getAutoWrapStyle(Workbook wb) {
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        return cellStyle;
    }


    /**
     * 设置单元格字体居中、并设置字体颜色
     * @param wb
     * @param fontSize
     * @param color
     * @return
     */
    public static CellStyle getFontStyle(Workbook wb, int fontSize, short color) {
        CellStyle cellStyle = wb.createCellStyle();
        Font font = setFont(wb, fontSize, color);
        center(cellStyle);
        cellStyle.setFont(font);
        return cellStyle;
    }


    /**
     * 设置单元格字体
     * @param wb
     * @param fontSize
     * @param color
     * @return
     */
    public static Font setFont(Workbook wb, int fontSize, short color) {
        //设置字体
        Font font = wb.createFont();
        font.setColor(color);
        font.setFontHeightInPoints((short) fontSize);
        return font;
    }

    public static Font setFont(Workbook wb, int fontSize) {
        //设置字体
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) fontSize);
        return font;
    }

    /**
     * 设置cell边框
     * @param workbook
     * @return
     */
    public static CellStyle setCellBorder(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        // 设置了边框属性
        // 下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        // 左边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        // 上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        // 右边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        //设置边框颜色黑色
        cellStyle.setTopBorderColor(IndexedColors.BLACK.index);
        cellStyle.setBottomBorderColor(IndexedColors.BLACK.index);
        cellStyle.setLeftBorderColor(IndexedColors.BLACK.index);
        cellStyle.setRightBorderColor(IndexedColors.BLACK.index);
        return cellStyle;
    }
}
