package com.bazinga.component;


import com.bazinga.dto.*;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.ThsBlockInfoService;
import com.bazinga.replay.service.ThsBlockStockDetailService;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.google.common.collect.Lists;
import com.tradex.util.Conf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SynExcelComponent {
    @Autowired
    private OtherBuyStockComponent otherBuyStockComponent;
    @Autowired
    private RealBuyOrSellComponent realBuyOrSellComponent;
    @Autowired
    private StockGraphComponent stockGraphComponent;
    @Autowired
    private HotBlockBestBuyComponent hotBlockBestBuyComponent;
    @Autowired
    private ZhuanZaiComponent zhuanZaiComponent;
    @Autowired
    private ThsDataUtilComponent thsDataUtilComponent;

    public void otherStockBuy() {
        List<OtherExcelDTO> list = Lists.newArrayList();
        File file = new File("D:\\circulate\\other.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\other.xlsx") + "不存在");
        }
        try {
            List<OtherExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(OtherExcelDTO.class);
            dataList.forEach(item -> {
                Date tradeDate = null;
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);//MMM dd hh:mm:ss Z yyyy
                try {
                    tradeDate = sdf.parse(item.getTradeDate());
                } catch (ParseException ex) {
                }
                String format = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
                item.setTradeDate(format);
                list.add(item);
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
        otherBuyStockComponent.zgcBuy(list);
    }



    public void ziDongHuaBuy() {
        List<ZiDongHuaDTO> list = Lists.newArrayList();
        File file = new File("D:\\circulate\\zidonghua.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\zidonghua.xlsx") + "不存在");
        }
        try {
            List<ZiDongHuaDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(ZiDongHuaDTO.class);
            dataList.forEach(item -> {
                Date tradeDate = null;
                Date buyDate = null;
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);//MMM dd hh:mm:ss Z yyyy
                try {
                    tradeDate = sdf.parse(item.getTradeDate());
                    buyDate = sdf.parse(item.getBuyTime());
                } catch (ParseException ex) {
                }
                String format = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
                String buyDateFormat = DateUtil.format(buyDate, DateUtil.HH_MM);
                item.setTradeDate(format);
                item.setBuyTime(buyDateFormat);
                list.add(item);
            });
            realBuyOrSellComponent.test(dataList);
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }

    }

    public void graphBuy() {
        List<ZiDongHuaDTO> list = Lists.newArrayList();
        File file = new File("D:\\circulate\\zidonghua.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\zidonghua.xlsx") + "不存在");
        }
        try {
            List<ZiDongHuaDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(ZiDongHuaDTO.class);
            dataList.forEach(item -> {
                Date tradeDate = null;
                Date buyDate = null;
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);//MMM dd hh:mm:ss Z yyyy
                try {
                    tradeDate = sdf.parse(item.getTradeDate());
                    buyDate = sdf.parse(item.getBuyTime());
                } catch (ParseException ex) {
                }
                String format = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
                String buyDateFormat = DateUtil.format(buyDate, DateUtil.HH_MM);
                item.setTradeDate(format);
                item.setBuyTime(buyDateFormat);
                list.add(item);
            });
            stockGraphComponent.graphBuy(dataList);
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }

    }

    public void hotBlockDrop() {
        List<HotBlockDropBuyExcelDTO> list = Lists.newArrayList();
        File file = new File("D:\\circulate\\hotDrop.xls");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\hotDrop.xls") + "不存在");
        }
        try {
            List<HotBlockDropBuyExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(HotBlockDropBuyExcelDTO.class);
            dataList.forEach(item -> {
                list.add(item);
            });
            hotBlockBestBuyComponent.hotBlockBestBuy(list);
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }

    }


    public void hotBlockDropScore() {
        List<HotBlockDropBuyExcelDTO> list = Lists.newArrayList();
        File file = new File("D:\\circulate\\hotDrop.xls");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\hotDrop.xls") + "不存在");
        }
        try {
            List<HotBlockDropBuyExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(HotBlockDropBuyExcelDTO.class);
            dataList.forEach(item -> {
                list.add(item);
            });
            hotBlockBestBuyComponent.hotBlockBestBuy(list);
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }

    }


    public void zhuanZaiBuy() {
        File file = new File("D:\\circulate\\zhuanzai.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\zhuanzai.xlsx") + "不存在");
        }
        try {
            List<ZhuanZaiExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(ZhuanZaiExcelDTO.class);
            zhuanZaiComponent.zhuanZaiBuy(dataList);
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }

    }

    public void zhuanZaiQuoteInfo() {
        File file = new File("D:\\circulate\\zhuanzai.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + Conf.get("D:\\circulate\\zhuanzai.xlsx") + "不存在");
        }
        try {
            List<ZhuanZaiExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(ZhuanZaiExcelDTO.class);
            thsDataUtilComponent.zhuanZaiStocks(dataList);
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }

    }


}
