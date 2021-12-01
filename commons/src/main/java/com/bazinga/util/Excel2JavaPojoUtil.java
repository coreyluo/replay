package com.bazinga.util;


import com.bazinga.annotation.ExcelElement;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.*;

/**
 * 可以将excel转换为java对象
 *
 * @author muxin
 * @modifier zixiao
 *
 */
public final class Excel2JavaPojoUtil {

    private static final Logger logger = LoggerFactory.getLogger(Excel2JavaPojoUtil.class);

    private Workbook wb = null;

    private Map<String, Integer> headerMap = new HashMap<String, Integer>();// 列名集合

    public Excel2JavaPojoUtil(File file) throws IOException, InvalidFormatException {
        wb = WorkbookFactory.create(file);
    }

    public Excel2JavaPojoUtil(InputStream inp) throws IOException, InvalidFormatException {
        wb = WorkbookFactory.create(inp);
    }

    public Row getRow(int row){
        return  wb.getSheetAt(0).getRow(row);
    }

    /**
     * Excel文件到POJO的转换: <br>
     *
     * @param clazz
     *            需要转换的类型。
     * @return 结果集合。
     * @throws Exception
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public <T> List<T> excel2JavaPojo(Class<T> clazz) throws Exception {
        List<T> results = new ArrayList<T>();
        if (clazz == null) {
            return results;
        }
        int columnNum = 0;
        Sheet sheet = wb.getSheetAt(0);
        if (sheet.getRow(0) != null) {
            columnNum = sheet.getRow(0).getLastCellNum() - sheet.getRow(0).getFirstCellNum();
        }
        try {
            if (columnNum > 0) {
                int n = 0;
                for (Row row : sheet) {
                    List<Object> rowList = new ArrayList<Object>();
                    for (int i = 0; i < columnNum; i++) {
                        Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                        Object o = transform(cell);
                        if (n == 0) {// 将表头信息放入map中
                            headerMap.put(String.valueOf(o), i);
                        } else {
                            rowList.add(o);
                        }
                    }
                    if (n >= 1 && !isEmpty(rowList)) {
                        value2Vo(clazz, results, rowList, n);
                    }
                    n++;
                }
            }
        } catch (Exception e) {
            logger.error("Excel文件到POJO的转换失败", e);
            // 解析失败，清空之前的结果
            results.clear();
            throw e;
        }
        return results;
    }

    /**
     * Excel文件到POJO的转换: <br>
     *
     * @param clazz   需要转换的类型。
     * @param lineNum 表头所处行数。
     * @return 结果集合。
     * @throws Exception
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    public <T> List<T> excel2JavaPojoFromLineNum(Class<T> clazz, int lineNum) throws Exception {
        List<T> results = new ArrayList<T>();
        if (clazz == null) {
            return results;
        }
        int columnNum = 0;
        Sheet sheet = wb.getSheetAt(0);
        if (sheet.getRow(lineNum) != null) {
            columnNum = sheet.getRow(lineNum).getLastCellNum() - sheet.getRow(lineNum).getFirstCellNum();
        }
        try {
            if (columnNum > 0) {
                int n = 0;
                int index = 0;
                for (Row row : sheet) {
                    if (index < lineNum) {
                        index++;
                        continue;
                    }

                    List<Object> rowList = new ArrayList<Object>();
                    for (int i = 0; i < columnNum; i++) {
                        Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                        Object o = transform(cell);
                        if (n == 0) {// 将表头信息放入map中
                            headerMap.put(String.valueOf(o), i);
                        } else {
                            rowList.add(o);
                        }
                    }
                    if (n >= 1 && !isEmpty(rowList)) {
                        value2Vo(clazz, results, rowList, n);
                    }
                    n++;
                }
            }
        } catch (Exception e) {
            logger.error("Excel文件到POJO的转换失败", e);
            // 解析失败，清空之前的结果
            results.clear();
            throw e;
        }
        return results;
    }


    /**
     * 根据注解和列名匹配，将匹配到的值放入返回list中
     *
     * @param clazz
     * @param results
     * @param rowList
     * @param n
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws Exception
     */
    private <T> void value2Vo(Class<T> clazz, List<T> results, List<Object> rowList, int n)
            throws IllegalArgumentException, ClassCastException, InstantiationException, IllegalAccessException {
        ExcelElement excelElement = null;
        String fieldName = null;
        Object value = null;
        Class<?> type = null;
        T t = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            excelElement = field.getAnnotation(ExcelElement.class);
            if (excelElement != null) {
                field.setAccessible(true);
                fieldName = excelElement.value();
                if (!headerMap.containsKey(fieldName)) {
                    continue;
                }
                try {
                    Integer num = headerMap.get(fieldName);
                    value = rowList.get(num);
                    boolean notNull = excelElement.notNull();// 检查是否为必填字段
                    if (value == null && notNull) {// 字段不能为空时
                        logger.info("第" + n + "行，没找到{} 对应的值。", new String[] { fieldName });
                        throw new IllegalArgumentException("第" + n + "行,未找到'" + fieldName + "'对应的值。");
                    }
                } catch (IllegalArgumentException e) {
                    logger.info("第" + n + "行，没找到{} 对应的值。", new String[] { fieldName });
                    throw new IllegalArgumentException("第" + n + "行,未找到'" + fieldName + "'对应的值。");
                }
                type = field.getType();
                try {
                    field.set(t, typeChange(type, value, fieldName, excelElement.pattern()));
                } catch (Exception e) {
                    logger.info("第" + n + "行，无效的 {} : {},可能指定类型与申明的类型不一致。", fieldName, value);
                    throw new ClassCastException("第" + n + "行，无效的 " + fieldName + " : " + value + ",可能指定类型与申明的类型不一致。");
                }
            }
        }
        results.add(t);
    }

    public static <T> T value2Vo(Class<T> clazz, List<Object> rowList, List<String> headerList) throws Exception {
        ExcelElement excelElement = null;
        String fieldName = null;
        Object value = null;
        Class<?> type = null;
        T t = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            excelElement = field.getAnnotation(ExcelElement.class);
            if (excelElement != null) {
                field.setAccessible(true);
                fieldName = excelElement.value();
                for (int index = 0; index < headerList.size(); index++) {
                    if (headerList.get(index).equals(fieldName)) {
                        value = rowList.get(index);
                        type = field.getType();
                        field.set(t, typeChange(type, value, fieldName, excelElement.pattern()));
                    }
                }
            }
        }
        return t;
    }

    /**
     * 转换excel单元格信息
     *
     * @param cell
     * @return
     */
    public Object transform(Cell cell) {
        Object o;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                o = null;
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                o = cell.getBooleanCellValue();
                break;
            // 数值
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    o = cell.getDateCellValue();
                } else {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    String temp = cell.getStringCellValue();
                    // 判断是否包含小数点，如果不含小数点，则以字符串读取，如果含小数点，则转换为Double类型的字符串
                    if (temp.indexOf(".") > -1) {
                        o = new Double(temp);
                    } else {
                        o = temp.trim();
                    }
                }
                break;
            case Cell.CELL_TYPE_STRING:
                o = cell.getStringCellValue().trim();
                break;
            case Cell.CELL_TYPE_ERROR:
                o = null;
                break;
            case Cell.CELL_TYPE_FORMULA:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                String str = cell.getStringCellValue();
                if (str != null) {
                    str = str.replaceAll("#N/A", "").trim();
                }
                o = str;
                break;
            default:
                o = null;
                break;
        }
        return o;
    }

    /**
     * 根据注解类型，转换单元格字段
     *
     * @param clazz
     * @param value
     * @param name
     * @param pattern
     * @return
     * @throws ClassCastException
     */
    private static Object typeChange(Class<?> clazz, Object value, String name, String pattern)
            throws ClassCastException {
        try {
            if (value == null) {
                return null;
            }
            String valueStr = String.valueOf(value);
            if(StringUtils.isBlank(valueStr)){
                return null;
            }
            if (String.class.equals(clazz)) {
                return String.valueOf(value);
            }
            if (Byte.class.equals(clazz)) {
                return new Byte(String.valueOf(value));
            }
            if (Short.class.equals(clazz)) {
                return new Short(String.valueOf(value));
            }
            if (Integer.class.equals(clazz)) {
                return new Integer(String.valueOf(value));
            }
            if (Long.class.equals(clazz)) {
                return new Long(String.valueOf(value));
            }
            if (Float.class.equals(clazz)) {
                return new Float(String.valueOf(value));
            }
            if (Double.class.equals(clazz)) {
                return new Double(String.valueOf(value));
            }
            if (Boolean.class.equals(clazz)) {
                return new Boolean(String.valueOf(value));
            }
            if (BigDecimal.class.equals(clazz)) {
                return new BigDecimal(String.valueOf(value));
            }
            if (Date.class.equals(clazz)) {
                if(value instanceof Date) {
                    return value;
                }else {
                    return DateFormatUtils.tryParse(value.toString());
                }
            }
        } catch (Exception e) {
            logger.info("'{}'的值'{}',无效", name, value);
            throw new ClassCastException("值无效，类型转换失败！"+e.toString());
        }
        return null;
    }

    /**
     * 判断一行excel单元格是否为空
     *
     * @param list
     * @return
     */
    private <T> boolean isEmpty(List<T> list) {
        if (list.isEmpty() && list.size() == 0) {
            return true;
        }
        int n = 0;
        for (T t : list) {
            if (t == null) {
                n++;
            }
        }
        if (n == list.size()) {
            return true;
        }
        return false;
    }
}
