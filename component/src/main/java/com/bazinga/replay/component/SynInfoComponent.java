package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.exception.BusinessException;
import com.bazinga.replay.dto.CirculateInfoExcelDTO;
import com.bazinga.replay.dto.HSTechExcelDTO;
import com.bazinga.replay.dto.TransferableBondInfoExcelDTO;
import com.bazinga.replay.dto.ZiDingYiBlockInfoExcelDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.CommonUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.MarketUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tradex.util.Conf;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author yunshan
 * @date 2019/5/13
 * 同步信息组件
 */
@Component
@Slf4j
public class SynInfoComponent {

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private BlockInfoService blockInfoService;

    @Autowired
    private BlockStockDetailService blockStockDetailService;

    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;

    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private HotCirculateInfoService hotCirculateInfoService;
    @Autowired
    private TransferableBondInfoService transferableBondInfoService;
    @Autowired
    private MarketInfoService marketInfoService;
    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private StockFactorService stockFactorService;

    public static List<String> BLOCK_NAME_FILTER_LIST = Lists.newArrayList("沪股通","深股通","标普道琼斯","新股","次新"
            ,"创业板重组松绑","高送转","填权","共同富裕示范区","融资融券","MSCI","ST");


    public void synStockFactor(int factorIndex) throws IOException {
        File file = new File("D:/circulate/thsindex"+ factorIndex +"_202201-202206.txt");
        List<String> list = FileUtils.readLines(file, "UTF-8");



        for (int i = 1; i <list.size(); i++) {
            String line = list.get(i);

            String[] array = line.split("\\|");
            if(array.length<4){
                log.info("行数i{} factorIndex{}", i,factorIndex);
                continue;
            }
            String uniqueKey = array[0] + SymbolConstants.UNDERLINE + array[1];
            if(factorIndex==1){
                String  kbarDate =  array[0];
                StockFactor stockFactor = new StockFactor();
                stockFactor.setKbarDate(kbarDate);
                stockFactor.setStockCode(array[1]);
                stockFactor.setStockName(array[2]);
                stockFactor.setIndex1(new BigDecimal(array[3]));
                stockFactor.setUniqueKey(uniqueKey);
                stockFactorService.save(stockFactor);
            }else {
                StockFactor stockFactor = stockFactorService.getByUniqueKey(uniqueKey);
                boolean saveFlag = false;
                if(stockFactor==null){
                    stockFactor = new StockFactor();
                    stockFactor.setKbarDate(array[0]);
                    stockFactor.setStockCode(array[1]);
                    stockFactor.setStockName(array[2]);
                    stockFactor.setIndex1(new BigDecimal(array[3]));
                    stockFactor.setUniqueKey(uniqueKey);
                    saveFlag = true;
                }
                if(factorIndex==2){
                    stockFactor.setIndex2a(new BigDecimal(array[3]));
                    stockFactor.setIndex2b(new BigDecimal(array[4]));
                    stockFactor.setIndex2c(new BigDecimal(array[5]));
                }else if(factorIndex==3){
                    stockFactor.setIndex3(new BigDecimal(array[3]));
                }else if(factorIndex==4){
                    stockFactor.setIndex4(new BigDecimal(array[3]));
                }else if(factorIndex==5){
                    stockFactor.setIndex5(new BigDecimal(array[3]));
                }else if(factorIndex==6){
                    stockFactor.setIndex6(new BigDecimal(array[3]));
                }else if(factorIndex==7){
                    stockFactor.setIndex7(new BigDecimal(array[3]));
                }
                if (saveFlag){
                    stockFactorService.save(stockFactor);
                }else {
                    stockFactorService.updateById(stockFactor);
                }
            }
        }

    }

    public void synThsBlockInfo() throws IOException {
        File file = new File("D:/circulate/block_conception.ini");
        List<String> list = FileUtils.readLines(file, "GBK");
        log.info(JSONObject.toJSONString(list));
        int blockIndex = 0;
        int detailIndex = 0;

        Map<String,String> blockInfoMap = Maps.newHashMap();
        Map<String,String> blockDetailMap = Maps.newHashMap();
        for (int i = 0; i < list.size(); i++) {
            if("[BLOCK_NAME_MAP_TABLE]".equals(list.get(i))){
                blockIndex = i;
            }
            if("[BLOCK_STOCK_CONTEXT]".equals(list.get(i))){
                detailIndex = i;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if(i>blockIndex && i< detailIndex){
                String blockString = list.get(i);
                String[] blockMapStr = blockString.split(SymbolConstants.EQUAL);
                String mapValue = blockMapStr.length==2 ? blockMapStr[1] : "";
                blockInfoMap.put(blockMapStr[0],mapValue);
            }
            if(i > detailIndex){
                String detailString = list.get(i);
                String[] detailMapStr = detailString.split(SymbolConstants.EQUAL);
                String mapValue = detailMapStr.length==2? detailMapStr[1]:"";
                blockDetailMap.put(detailMapStr[0],mapValue);
            }
        }
        log.info("板块数据{}",JSONObject.toJSONString(blockInfoMap));
        block: for (Map.Entry<String, String> entry : blockInfoMap.entrySet()) {
            String detailString = blockDetailMap.get(entry.getKey());
            if(StringUtils.isEmpty(detailString)){
                continue;
            }
            for (String filterName : BLOCK_NAME_FILTER_LIST) {
                if(entry.getValue().contains(filterName)){
                    continue block;
                }
            }

            String[] detailArr = detailString.split(SymbolConstants.COMMA);
            Set<String> detailSet = Sets.newHashSet(detailArr);
            ThsBlockInfo blockInfo = new ThsBlockInfo();
            blockInfo.setBlockCode(entry.getKey());
            blockInfo.setBlockName(entry.getValue());
            blockInfo.setTotalCount(detailSet.size());
            thsBlockInfoService.save(blockInfo);
            for (String detail : detailSet) {
                ThsBlockStockDetail thsBlockStockDetail = new ThsBlockStockDetail();
                thsBlockStockDetail.setBlockCode(entry.getKey());
                thsBlockStockDetail.setBlockName(entry.getValue());
                thsBlockStockDetail.setStockCode(  detail.split(":")[1]);
                CirculateInfoQuery query = new CirculateInfoQuery();
                query.setStockCode(thsBlockStockDetail.getStockCode());
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(query);
                if(CollectionUtils.isEmpty(circulateInfos)){
                    thsBlockStockDetail.setStockName("");
                }else {
                    thsBlockStockDetail.setStockName(circulateInfos.get(0).getStockName());
                }
                thsBlockStockDetailService.save(thsBlockStockDetail);
            }
        }
    }

    public void thsblockIndex() throws IOException {
        File file = new File("D:/circulate/同花顺行业板块索引.txt");
        List<String> list = FileUtils.readLines(file, "UTF-8");

        for (String blockIndexString : list) {
            log.info("行数据{}",blockIndexString);
            String[] indexAttr = blockIndexString.split("\\t");
            if(indexAttr.length>2){
                int count = 0;
                String blockCode = indexAttr[0];
                String blockName = indexAttr[1];
                for (int i = 2; i < indexAttr.length; i++) {
                    ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
                    String originalBlockCode = indexAttr[i];
                    query.setBlockCode(originalBlockCode);
                    List<ThsBlockStockDetail> thsBlockStockDetails = thsBlockStockDetailService.listByCondition(query);
                    if(CollectionUtils.isEmpty(thsBlockStockDetails)){
                        continue;
                    }
                    for (ThsBlockStockDetail thsBlockStockDetail : thsBlockStockDetails) {
                        thsBlockStockDetail.setBlockCode(blockCode);
                        thsBlockStockDetail.setBlockName(blockName);
                        thsBlockStockDetailService.updateById(thsBlockStockDetail);
                        count++;
                    }


                }
                ThsBlockInfo thsBlockInfo = new ThsBlockInfo();
                thsBlockInfo.setTotalCount(count);
                thsBlockInfo.setBlockName(blockName);
                thsBlockInfo.setBlockCode(blockCode);
                log.info("新增板块数据 blockCode{} blockName{}",blockCode,blockName);
                thsBlockInfoService.save(thsBlockInfo);
            }
        }

    }
    /**
     * 同步板块信息
     */
    public   void synBlockInfo() throws IOException {
        File file = new File("D:/circulate/gn_block.txt");
        List<String> list = FileUtils.readLines(file, "GBK");
        list.forEach(item -> {
            String[] contents = item.split("\t");
            if (contents.length < 3) {
                throw new IllegalArgumentException("原始数据异常");
            }
            BlockInfoQuery blockInfoQuery = new BlockInfoQuery();
            String blockCode = contents[1].trim();
            blockInfoQuery.setBlockCode(contents[1].trim());
            List<BlockInfo> blockInfos = blockInfoService.listByCondition(blockInfoQuery);
            if (CollectionUtils.isNotEmpty(blockInfos)) {
                BlockInfo blockInfo = blockInfos.get(0);
                blockInfo.setBlockCode(blockCode);
                blockInfo.setBlockName(contents[0]);
                blockInfo.setTotalCount(contents.length - 2);
                blockInfoService.updateById(blockInfo);
            } else {
                BlockInfo blockInfo = new BlockInfo();
                blockInfo.setBlockCode(blockCode);
                blockInfo.setBlockName(contents[0]);
                blockInfo.setTotalCount(contents.length - 2);
                blockInfoService.save(blockInfo);
            }
            blockStockDetailService.deleteByBlockCode(blockCode);
            for (int i = 2; i < contents.length; i++) {
                BlockStockDetail blockStockDetail = new BlockStockDetail();
                blockStockDetail.setBlockCode(blockCode);
                blockStockDetail.setBlockName(contents[0]);
                blockStockDetail.setStockCode(contents[i]);
                CirculateInfoQuery query = new CirculateInfoQuery();
                query.setStockCode(contents[i]);
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(query);
                if (!CollectionUtils.isEmpty(circulateInfos)) {
                    blockStockDetail.setStockName(circulateInfos.get(0).getStockName());
                }
                blockStockDetailService.save(blockStockDetail);
            }
            log.info("处理板块blockCode ={} blockName ={},个股数量={}", contents[1], contents[0], contents.length - 2);
        });
        log.info("处理完毕,处理总板块数量 size ={}", list.size());
    }

    public void synCirculateInfo() {
        File file = new File("D:/circulate/circulate.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/circulate.xlsx" + "不存在");
        }
        try {
            List<CirculateInfoExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(CirculateInfoExcelDTO.class);
            dataList.forEach(item -> {
                CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
                circulateInfoQuery.setStockCode(item.getStock());
                List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
                CirculateInfo circulateInfo = null;
                if (!CollectionUtils.isEmpty(circulateInfos)) {
                    circulateInfo = circulateInfos.get(0);
                }
                if((!item.getStock().equals("601099"))&&(!item.getStock().equals("600777"))) {
                    if (circulateInfo == null) {
                        circulateInfo = convert2Mode(item);
                        circulateInfoService.save(circulateInfo);
                    } else {
                        circulateInfo.setStockCode(item.getStock());
                        circulateInfo.setCirculate(item.getTotalQuantity().longValue());
                        circulateInfo.setCirculateZ(item.getCirculateZ().longValue());
                        circulateInfo.setStockName(item.getStockName());
                        circulateInfo.setStockType(CommonUtil.getStockType(item.getCirculateZ().longValue()));
                        circulateInfoService.updateById(circulateInfo);
                    }
                }
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }

    public void synHotCirculateInfo() {
        File file = new File("D:/circulate/hotCirculate.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/hotCirculate.xlsx" + "不存在");
        }
        try {
            List<CirculateInfoExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(CirculateInfoExcelDTO.class);
            dataList.forEach(item -> {
                HotCirculateInfoQuery query = new HotCirculateInfoQuery();
                query.setStockCode(item.getStock());
                List<HotCirculateInfo> hotCirculateInfos = hotCirculateInfoService.listByCondition(query);
                HotCirculateInfo hotCirculateInfo = null;
                if (!CollectionUtils.isEmpty(hotCirculateInfos)) {
                    hotCirculateInfo = hotCirculateInfos.get(0);
                }
                if (hotCirculateInfo==null) {
                    hotCirculateInfo = convert2HotCirculateMode(item);
                    hotCirculateInfoService.save(hotCirculateInfo);
                } else {
                    hotCirculateInfo.setStockCode(item.getStock());
                    hotCirculateInfo.setCirculate(item.getTotalQuantity().longValue());
                    hotCirculateInfo.setCirculateZ(item.getCirculateZ().longValue());
                    hotCirculateInfo.setStockName(item.getStockName());
                    hotCirculateInfo.setStockType(CommonUtil.getStockType(item.getCirculateZ().longValue()));
                    hotCirculateInfoService.updateById(hotCirculateInfo);
                }
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }

    public void synHSTECH() {
        File file = new File("D:/circulate/HK8007002.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/hotCirculate.csv" + "不存在");
        }
        try {
            List<HSTechExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(HSTechExcelDTO.class);
            dataList.forEach(item -> {
                String time = item.getTime();
                Date date = DateUtil.cstToDate(time);
                StockKbar stockKbar = new StockKbar();
                stockKbar.setStockCode("HSTECH");
                stockKbar.setStockName("恒生科技指数");
                stockKbar.setKbarDate(DateUtil.format(date,DateUtil.yyyyMMddHHmmss));
                stockKbar.setUniqueKey(stockKbar.getStockCode()+"_"+stockKbar.getKbarDate());
                stockKbar.setTradeQuantity(0l);
                stockKbar.setTradeAmount(new BigDecimal(0));
                stockKbar.setHighPrice(item.getHigh());
                stockKbar.setLowPrice(item.getLow());
                stockKbar.setOpenPrice(item.getOpen());
                stockKbar.setClosePrice(item.getClose());
                stockKbarService.save(stockKbar);
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }



    public void synMarketInfoZz500() {
        File file = new File("D:/circulate/zz500.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/zz500.xlsx" + "不存在");
        }
        try {
            List<CirculateInfoExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(CirculateInfoExcelDTO.class);
            dataList.forEach(item -> {
                MarketInfoQuery query = new MarketInfoQuery();
                query.setMarketCode("399905");
                query.setStockCode(item.getStock());
                List<MarketInfo> marketInfos = marketInfoService.listByCondition(query);
                if (CollectionUtils.isEmpty(marketInfos)) {
                    MarketInfo marketInfo = new MarketInfo();
                    marketInfo.setMarketCode("399905");
                    marketInfo.setStockCode(item.getStock());
                    marketInfo.setMarketName("中证500");
                    marketInfo.setCreateTime(new Date());
                    marketInfoService.save(marketInfo);
                }
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }

    public void synTbondInfo() {
        File file = new File("D:/circulate/tbond.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/tbond.xlsx" + "不存在");
        }
        try {
            List<TransferableBondInfoExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(TransferableBondInfoExcelDTO.class);
            dataList.forEach(item -> {
                TransferableBondInfo bondInfo = new TransferableBondInfo();
                bondInfo.setStockCode(item.getStockCode());
                bondInfo.setStockName(item.getStockName());
                bondInfo.setMainCode(item.getMainCode());
                bondInfo.setMainName(item.getMainName());
                bondInfo.setMarketValue(0L);
                transferableBondInfoService.save(bondInfo);

            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }


    public void synZiDingYiInfo() {
        File file = new File("D:/circulate/zidingyi.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/zidingyi.xlsx" + "不存在");
        }
        try {
            List<ZiDingYiBlockInfoExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(ZiDingYiBlockInfoExcelDTO.class);
            dataList.forEach(item -> {
                BlockStockDetail detail = new BlockStockDetail();
                detail.setBlockCode("999000");
                detail.setBlockName("自定义板块");
                detail.setStockCode(item.getStockCode());
                detail.setStockName(item.getStockName());
                detail.setCreateTime(new Date());
                blockStockDetailService.save(detail);
            });
            log.info("更新流通 z 信息完毕 size = {}", dataList.size());
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }


    private CirculateInfo convert2Mode(CirculateInfoExcelDTO item) {
        Integer marketCode = MarketUtil.getMarketCode(item.getStock());
        CirculateInfo circulateInfo = new CirculateInfo();
        circulateInfo.setStockCode(item.getStock());
        circulateInfo.setMarketType(marketCode);
        circulateInfo.setCirculate(item.getTotalQuantity().longValue());
        System.out.println(item.getStock());
        circulateInfo.setCirculateZ(item.getCirculateZ().longValue());
        circulateInfo.setStockName(item.getStockName());
        circulateInfo.setStockType(CommonUtil.getStockType(item.getCirculateZ().longValue()));
        return circulateInfo;
    }

    private HotCirculateInfo convert2HotCirculateMode(CirculateInfoExcelDTO item) {
        Integer marketCode = MarketUtil.getMarketCode(item.getStock());
        HotCirculateInfo circulateInfo = new HotCirculateInfo();
        circulateInfo.setStockCode(item.getStock());
        circulateInfo.setMarketType(marketCode);
        circulateInfo.setCirculate(item.getTotalQuantity().longValue());
        circulateInfo.setCirculateZ(item.getCirculateZ().longValue());
        circulateInfo.setStockName(item.getStockName());
        circulateInfo.setStockType(CommonUtil.getStockType(item.getCirculateZ().longValue()));
        return circulateInfo;
    }


}
