package com.service;

import com.crawler.Wrapper_gjdairqr001;
import com.alibaba.fastjson.JSON;
import com.controller.Response;
import com.dao.FlightInfoDAO;
import com.model.FlightInfo;
import com.qunar.qfwrapper.bean.search.*;
import com.qunar.qfwrapper.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by HeXi on 2018/4/5.
 */
@Service
public class SingleDstCrawlerService {

    private Wrapper_gjdairqr001 crawler = new Wrapper_gjdairqr001();

    private static final Logger logger = LoggerFactory.getLogger(SingleDstCrawlerService.class);

    private static final String FLIGHTNO_DILIMITER = ",";

    @Resource
    private FlightInfoDAO flightInfoDAO;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    // 阀值1
    private static final long threshold1 = 120L * 60L * 1000L;

    // 阀值2
    private static final long threshold2 = 1440L * 60L * 1000L;

    private List<FlightInfo> triggerCrawler(FlightSearchParam param) throws BizException {
        String html = crawler.getHtml(param);
        ProcessResultInfo processResultInfo = crawler.process(html, param);
        String status = processResultInfo.getStatus();
        if (StringUtils.equals(status, Constants.SOLD_OUT)) {
            throw new BizException(String.format("出发地: %s, 目的地：%s, 出发日期: %s, 乘客人数: %s，机票已售罄。", param.getDep(), param.getArr(), param.getDepDate(), param.getPassengerCount()));
        } else if (StringUtils.equals(status, Constants.INVALID_DATE)) {
            throw new BizException(String.format("出发地: %s, 目的地：%s, 出发日期: %s, 乘客人数: %s，过期时间。", param.getDep(), param.getArr(), param.getDepDate(), param.getPassengerCount()));
        } else if (StringUtils.equals(status, Constants.OTHER)) {
            throw new BizException("不明原因，请联系管理员!");
        }
        List<? extends BaseFlightInfo> data = processResultInfo.getData();
        if (data == null || data.size() <= 0) {
            throw new BizException(String.format("满足出发地：%s，目的地：%s，出发日期：%s，乘客人数 %s的航班不存在", param.getDep(), param.getArr(), param.getDepDate(), param.getSpecailBookingParam()));
        }
        final List<FlightInfo> toModifiedList = new ArrayList<>();
        for (BaseFlightInfo info : data) {
            FlightInfo flightInfo = new FlightInfo();
            FlightDetail detail = info.getDetail();
            flightInfo.setDepCity(detail.getDepcity());
            List<FlightSegement> flightSegementList = info.getInfo();
            flightInfo.setDepTime(flightSegementList.get(0).getDeptime());
            StringBuilder flightNoBuilder = new StringBuilder();
            for (FlightSegement segement : flightSegementList) {
                flightNoBuilder.append(segement.getFlightno()).append(FLIGHTNO_DILIMITER);
            }
            if (flightNoBuilder.length() > 0) {
                for (int i = 0; i < FLIGHTNO_DILIMITER.length(); i++) {
                    flightNoBuilder.deleteCharAt(flightNoBuilder.length() - 1);
                }
            }
            flightInfo.setSeat(Integer.parseInt(param.getPassengerCount()) + 300);
            flightInfo.setPrice(detail.getPrice()+detail.getTax());
            flightInfo.setFlightNo(flightNoBuilder.toString());
            flightInfo.setArrCity(detail.getArrcity());
            flightInfo.setDepDate(new SimpleDateFormat("yyyy-MM-dd").format(detail.getDepdate()));

            toModifiedList.add(flightInfo);
        }
        return toModifiedList;
    }

    public Response<List<FlightInfo>> search(final FlightSearchParam param) {
        Response<List<FlightInfo>> response = new Response<>();
        try {
            // checkParams
            Assert.isTrue(StringUtils.isNotBlank(param.getDep()), "出发地不能为空");//出发地
            Assert.isTrue(StringUtils.isNotBlank(param.getArr()), "目的地不能为空");//目的地
            Assert.isTrue(StringUtils.isNotBlank(param.getDepDate()), "出发日期不能为空");//出发日期
            Assert.isTrue(StringUtils.isNotBlank(param.getPassengerCount()), "乘客人数不能为空");//乘客人数
            int number = Integer.parseInt(param.getPassengerCount());
            String depDate = param.getDepDate();
            String[] split = depDate.split("-");
            if (split[1].length() <= 1) {
                split[1] = "0"+split[1];
            }
            param.setDepDate(split[0]+"-"+split[1]+"-"+split[2]);
            //查询数据库入库时间
            List<Date> createTimeList = flightInfoDAO.selectCreateTimeList(param.getDepDate(), param.getDep(), param.getArr());
            // 如果没有，开始爬
            if (createTimeList == null || createTimeList.size() <= 0) {
                // 触发爬虫
                final List<FlightInfo> toInsertList = this.triggerCrawler(param);

                // 插入新的数据（一个List）
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        int cnt = flightInfoDAO.insertFlightInfoList(toInsertList);
                        // 如果有插入失败的行，就执行如下补偿操作
                        if (cnt < toInsertList.size()) {
                            logger.info("insert flight info list cnt: {}", cnt);
                            for (FlightInfo flightInfo : toInsertList) {
                                cnt = flightInfoDAO.insertFlightInfo(flightInfo);
                                logger.info("补偿插入对象json格式：{}", JSON.toJSONString(flightInfo));
                                logger.info("补偿插入cnt: {}", cnt);
                            }
                        }
                    }
                });

                List<FlightInfo> flightInfoList = flightInfoDAO.selectSearchResult(param.getDep(), param.getArr(), param.getDepDate(), number);
                response.setData(flightInfoList);
                response.setSuccess(true);
                response.setMsg("处理成功!");
            } else {
                // 如果有
                List<FlightInfo> flightInfoList = flightInfoDAO.selectSearchResult(param.getDep(), param.getArr(), param.getDepDate(), number);
                long currentTimestamp = new Date().getTime();
                for (FlightInfo flightInfo : flightInfoList) {
                    long createTimestamp = flightInfo.getCreateTime().getTime();
                    long diff = Math.abs(currentTimestamp - createTimestamp);
                    boolean shouldTriggerCrawler = false;
                    if (diff > threshold2) {
                        // 返回空数据
                        response.setData(new ArrayList<>());
                        response.setMsg("正在处理新的爬虫");
                        response.setSuccess(true);
                        shouldTriggerCrawler = true;
                    } else if (diff < threshold1) {
                        response.setSuccess(true);
                        response.setData(flightInfoList);
                        response.setMsg("处理成功");
                    } else {
                        response.setSuccess(true);
                        response.setData(flightInfoList);
                        response.setMsg("处理成功");
                        shouldTriggerCrawler = true;
                    }
                    if (shouldTriggerCrawler) {
                        threadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                List<FlightInfo> toUpdateList = triggerCrawler(param);
                                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                                    @Override
                                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                                        for (FlightInfo info : toUpdateList) {
                                            int result = flightInfoDAO.updateSeatAndCreateTime(info);
                                            logger.info("thread: {}, update flight info result: {}", Thread.currentThread().getName(), result);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }

            }
        } catch (IllegalArgumentException e) {
            logger.error("参数校验异常", e);
            response.setMsg(e.getMessage());
            response.setSuccess(false);
            response.setData(new ArrayList<>());
            return response;
        } catch (BizException e) {
            logger.error("业务异常", e);
            response.setMsg(e.getBizErrorMsg());
            response.setSuccess(false);
            response.setData(new ArrayList<>());
            return response;
        } catch (Exception e) {
            logger.error("系统异常", e);
            response.setMsg("系统异常");
            response.setData(new ArrayList<>());
            return response;
        }
        return response;
    }

    public Response<Boolean> book(FlightSearchParam param) {
        Response<Boolean> response = new Response<>();
        try {
            Assert.isTrue(!ObjectUtils.isEmpty(param), "入参为空！");
            String queryId = param.getQueryId();
            Assert.isTrue(StringUtils.isNotBlank(queryId), "queryId不能为空");
            FlightInfo flightInfo = flightInfoDAO.selectFlightInfoById(Long.parseLong(queryId));
            if (flightInfo == null) {
                throw new BizException(String.format("id为：%s 的航班不存在", queryId));
            }

            //重新设置FlightSearchParam
            param.setQueryId(null);
            param.setDep(flightInfo.getDepCity());
            param.setArr(flightInfo.getArrCity());
            param.setDepDate(flightInfo.getDepDate());
            param.setTimeOut("60000");
            param.setWrapperid("gjdairqr00b");
            // 触发爬虫
            List<FlightInfo> flightInfoList = triggerCrawler(param);
            // 开始校验
            if (flightInfoList == null || flightInfoList.size() <= 0) {
                throw new BizException(String.format("flightNo: %s, depDate: %s 的爬虫校验出现异常, 没爬到相应的航班信息", flightInfo.getFlightNo(), flightInfo.getDepDate()));
            }
            // 找到对应的航班号，用flightNo和depDate唯一索引筛选出来
            FlightInfo updatedFlightInfo = null;
            for (FlightInfo info : flightInfoList) {
                if (StringUtils.equals(info.getFlightNo(), flightInfo.getFlightNo()) &&
                        StringUtils.equals(info.getDepDate(), flightInfo.getDepDate())) {
                    updatedFlightInfo = info;
                    break;
                }
            }
            // 不等于null，说明找到了，价格偏差不大，说明一致。
            if (updatedFlightInfo != null && (Math.abs(flightInfo.getPrice()-updatedFlightInfo.getPrice())<0.1)) {
                response.setData(true);
                response.setSuccess(true);
                response.setMsg("校验一致");
            } else {
                response.setMsg("校验不一致");
            }
        } catch (IllegalArgumentException e) {
            logger.error("参数校验异常", e);
            response.setMsg(e.getMessage());
            response.setSuccess(false);
            return response;
        } catch (BizException e) {
            logger.error("业务异常", e);
            response.setMsg(e.getBizErrorMsg());
            response.setSuccess(false);
            return response;
        } catch (Exception e) {
            logger.error("系统异常", e);
            response.setMsg("系统异常");
            return response;
        }
        return response;
    }

    public Response<Boolean> order(FlightSearchParam param) {
//        Response<Boolean> response = new Response<>();
//        try {
//            // TODO: 2018/4/7
//        } catch (IllegalArgumentException e) {
//            logger.error("参数校验异常", e);
//            response.setMsg(e.getMessage());
//            response.setSuccess(false);
//            response.setData(false);
//            return response;
//        } catch (BizException e) {
//            logger.error("业务异常", e);
//            response.setMsg(e.getBizErrorMsg());
//            response.setSuccess(false);
//            return response;
//        } catch (Exception e) {
//            logger.error("系统异常", e);
//            response.setMsg("系统异常");
//            return response;
//        }
//        return response;
        return book(param);
    }
}
