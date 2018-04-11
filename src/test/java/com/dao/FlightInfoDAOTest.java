package com.dao;

import com.model.FlightInfo;
import com.service.SingleDstCrawlerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by HeXi on 2018/4/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml","classpath:spring-*.xml"}) //加载配置文件
public class FlightInfoDAOTest {

    @Resource
    private FlightInfoDAO flightInfoDAO;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final Logger logger = LoggerFactory.getLogger(FlightInfoDAOTest.class);

    private long id;
    private String depCity = "depCity";
    private String arrCity = "arrCity";
    private String depDate = "2018-04-07";
    private String flightNo = "ABC,EDG";
    private String depTime = "22:47";
    private double price = 2345.5;
    private int seat = 10;
    private Date createTime;

    @Before
    public void setup() {
        flightInfoDAO.deleteFlightInfoByDepDateAndFlightNo(depDate, flightNo);
    }

    @After
    public void after() {
        flightInfoDAO.deleteFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        flightInfoDAO.deleteFlightInfoById(id);
    }

    @Resource
    private SingleDstCrawlerService service;

    @Test
    public void testService() throws Exception {
        Assert.assertNotNull(service);
    }

    @Test
    public void selectCreateTime() throws Exception {
        insertFlightInfo();
        Date createTime = flightInfoDAO.selectCreateTime(depDate, flightNo);
        long time = createTime.getTime();
        logger.info("select createTime: {}", time);
        logger.info("this.createTime: {}", this.createTime.getTime());
        Assert.assertTrue(Math.abs(this.createTime.getTime() / 1000 - time / 1000) < 5);
    }

    @Test
    public void insertFlightInfo() throws Exception {
        FlightInfo flightInfo = new FlightInfo();
        flightInfo.setDepCity(depCity);
        flightInfo.setArrCity(arrCity);
        flightInfo.setDepDate(depDate);
        flightInfo.setFlightNo(flightNo);
        flightInfo.setDepTime(depTime);
        flightInfo.setPrice(price);
        flightInfo.setSeat(seat);

        Assert.assertEquals(1, flightInfoDAO.insertFlightInfo(flightInfo));

        flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        Assert.assertEquals(depCity, flightInfo.getDepCity());
        Assert.assertEquals(arrCity, flightInfo.getArrCity());
        Assert.assertEquals(depDate, flightInfo.getDepDate());
        Assert.assertEquals(flightNo, flightInfo.getFlightNo());
        Assert.assertEquals(depTime, flightInfo.getDepTime());
        Assert.assertTrue(price==flightInfo.getPrice());
        Assert.assertEquals(seat, flightInfo.getSeat());
        id = flightInfo.getId();
        createTime = flightInfo.getCreateTime();
    }

    @Test
    public void insertFlightInfoList() throws Exception {
        FlightInfo flightInfo = new FlightInfo();
        flightInfo.setDepCity(depCity);
        flightInfo.setArrCity(arrCity);
        flightInfo.setDepDate(depDate);
        flightInfo.setFlightNo(flightNo);
        flightInfo.setDepTime(depTime);
        flightInfo.setPrice(price);
        flightInfo.setSeat(seat);

        List<FlightInfo> flightInfoList = new ArrayList<>();
        flightInfoList.add(flightInfo);

        Assert.assertEquals(1, flightInfoDAO.insertFlightInfoList(flightInfoList));

    }

    @Test
    public void insertFlightInfoListInTransaction() throws Exception {
        FlightInfo flightInfo = new FlightInfo();
        flightInfo.setDepCity(depCity);
        flightInfo.setArrCity(arrCity);
        flightInfo.setDepDate(depDate);
        flightInfo.setFlightNo(flightNo);
        flightInfo.setDepTime(depTime);
        flightInfo.setPrice(price);
        flightInfo.setSeat(seat);

        List<FlightInfo> flightInfoList = new ArrayList<>();
        flightInfoList.add(flightInfo);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Assert.assertEquals(1, flightInfoDAO.insertFlightInfoList(flightInfoList));
            }
        });
    }

    @Test
    public void updateFlightInfo() throws Exception {
        FlightInfo flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        Assert.assertTrue(flightInfo == null);
        insertFlightInfo();
        flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);

        String newDepCity  = "NewYork";
        String newArrCity  = "California";
        String newDepDate  = "someday";
        String newFlightNo = "Astronaut";
        String newDepTime  = "11:11";
        double newPrice    = 12.0D;
        int    newSeat     = 1;
        Date   newDate     = new Date();

        flightInfo.setDepCity(newDepCity);
        flightInfo.setArrCity(newArrCity);
        flightInfo.setDepDate(newDepDate);
        flightInfo.setFlightNo(newFlightNo);
        flightInfo.setDepTime(newDepTime);
        flightInfo.setPrice(newPrice);
        flightInfo.setSeat(newSeat);
        flightInfo.setCreateTime(newDate);

        Assert.assertEquals(1, flightInfoDAO.updateFlightInfo(flightInfo));

        flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(newDepDate, newFlightNo);
        Assert.assertEquals(newDepCity, flightInfo.getDepCity());
        Assert.assertEquals(newArrCity, flightInfo.getArrCity());
        Assert.assertEquals(newDepDate, flightInfo.getDepDate());
        Assert.assertEquals(newFlightNo, flightInfo.getFlightNo());
        Assert.assertEquals(newDepTime, flightInfo.getDepTime());
        Assert.assertTrue(newPrice==flightInfo.getPrice());
        Assert.assertEquals(newSeat, flightInfo.getSeat());
        Assert.assertTrue(Math.abs(newDate.getTime() / 1000-flightInfo.getCreateTime().getTime()/1000) < 5);
    }

    @Test
    public void updateSeatDown() throws Exception {
        FlightInfo flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        Assert.assertTrue(flightInfo == null);
        insertFlightInfo();
        flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        int delta = 2;
        flightInfoDAO.updateSeatDown(flightInfo.getId(), delta);
        flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        Assert.assertEquals(seat - delta, flightInfo.getSeat());
    }

    @Test
    public void selectFlightInfo() throws Exception {
        FlightInfo flightInfo = flightInfoDAO.selectFlightInfoByDepDateAndFlightNo(depDate, flightNo);
        Assert.assertTrue(flightInfo == null);
        insertFlightInfo();
    }

    @Test
    public void selectSearchResult() throws Exception {
        List<FlightInfo> flightInfos = flightInfoDAO.selectSearchResult(depCity, arrCity, depDate, 2);
        Assert.assertNotNull(flightInfos);
        Assert.assertEquals(0, flightInfos.size());
        insertFlightInfo();
        flightInfos = flightInfoDAO.selectSearchResult(depCity, arrCity, depDate, 2);
        Assert.assertNotNull(flightInfos);
        Assert.assertEquals(1, flightInfos.size());

        flightInfos = flightInfoDAO.selectSearchResult(depCity, arrCity, depDate, seat +1);
        Assert.assertNotNull(flightInfos);
        Assert.assertEquals(0, flightInfos.size());
    }

}